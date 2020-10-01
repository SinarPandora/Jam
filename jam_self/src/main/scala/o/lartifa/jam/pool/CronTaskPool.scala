package o.lartifa.jam.pool

import java.util.concurrent.Executors

import cc.moecraft.logger.HyLogger
import cn.hutool.cron.CronUtil
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.tasks.JamCronTask.TaskDefinition
import o.lartifa.jam.model.tasks.{ChangeRespFrequency, GoASleep, JamCronTask, WakeUp}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.plugins.picbot.FetchPictureTask
import o.lartifa.jam.pool.CronTaskPool.logger

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

/**
 * 定时任务池
 *
 * Author: sinar
 * 2020/1/25 13:32
 */
class CronTaskPool() {

  private var _taskDefinition: Map[String, TaskDefinition] = Map.empty
  private val runningTasks: mutable.Map[String, ListBuffer[JamCronTask]] = mutable.Map.empty

  /**
   * 获取任务定义
   *
   * @return 任务定义
   */
  def taskDefinition: Map[String, TaskDefinition] = _taskDefinition

  /**
   * 自动刷新任务定义
   * 该操作会强制停止当前全部任务（等待任务完成）
   */
  def autoRefreshTaskDefinition(): CronTaskPool = {
    logger.log("正在刷新定时任务定义，运行中的全部任务将被取消")
    this.removeAll()
    if (CronUtil.getScheduler.isStarted) CronUtil.stop()
    this._taskDefinition = Map(
      "回复频率变更" -> TaskDefinition("回复频率变更", classOf[ChangeRespFrequency], isSingleton = false),
      "睡眠" -> TaskDefinition("睡眠", classOf[GoASleep], isSingleton = true),
      "起床" -> TaskDefinition("起床", classOf[WakeUp], isSingleton = true),
      "更新图片库" -> TaskDefinition("更新图片库", classOf[FetchPictureTask], isSingleton = true)
    ) ++ JamPluginLoader.loadedComponents.cronTaskDefinitions.map(it => it.name -> it)
    CronUtil.start()
    this._taskDefinition.values.foreach(_.startRequireTasks())
    logger.log("定时任务定义刷新完成")
    this
  }

  /**
   * 添加定时任务到定时任务池
   *
   * @param task 任务信息组
   */
  def add(task: JamCronTask): Unit = {
    runningTasks.getOrElseUpdate(task.name, ListBuffer.empty).addOne(task)
  }

  /**
   * 添加全部任务到定时任务池
   *
   * @param task 任务信息组列表
   */
  def addAll(task: Seq[JamCronTask]): Unit = {
    task.groupBy(_.name).foreach { case (name, tasksWithSameName) =>
      runningTasks.getOrElseUpdate(name, ListBuffer.empty).addAll(tasksWithSameName)
    }
  }

  /**
   * 获取全部同名定时任务
   *
   * @param name 任务名
   * @return 任务列表
   */
  def getAll(name: String): List[JamCronTask] = {
    runningTasks.getOrElse(name, ListBuffer.empty).filter(_.name == name).toList
  }

  /**
   * 获取当前会话下的全部同名任务
   *
   * @param name     任务名
   * @param chatInfo 会话信息
   * @return 任务列表
   */
  def getAll(name: String, chatInfo: ChatInfo): List[JamCronTask] = {
    getAll(name).filter(_.chatInfo == chatInfo)
  }

  /**
   * 删除指定定时任务
   *
   * @param task 要删除的任务
   * @return 删除结果
   */
  def remove(task: JamCronTask): Option[JamCronTask] = {
    runningTasks.get(task.name).flatMap { taskList =>
      taskList.find(_.id == task.id).map { task =>
        taskList -= task
        task
      }
    }
  }

  /**
   * 删除全部指定的定时任务
   *
   * @param list 要删除的任务列表
   */
  def removeAll(list: List[JamCronTask]): Unit = {
    list.groupBy(_.name).foreach { case (name, taskList) =>
      runningTasks.get(name).foreach(_ --= taskList)
    }
  }

  /**
   * 删除全部定时任务
   */
  def removeAll(): Unit = {
    runningTasks.flatMap(_._2).foreach(_.cancel(false))
    runningTasks.clear()
  }

  /**
   * 通过任务名获取唯一的定时任务
   *
   * @param name    任务名
   * @param context 任务执行上下文（可选）
   * @throws ExecutionException 执行异常，当定时任务不唯一时抛出
   * @return 定时任务
   */
  @throws[ExecutionException]
  def get(name: String)(implicit context: CommandExecuteContext = null): Option[JamCronTask] = {
    val searchPath = this.getAll(name)
    if (searchPath.lengthIs == 0) {
      None
    } else if (searchPath.lengthIs == 1) {
      searchPath.headOption
    } else if (context != null) {
      val chatInfo = context.chatInfo
      val tasks = searchPath.filter(_.chatInfo == chatInfo)
      if (tasks.lengthIs == 1) tasks.headOption
      else throw ExecutionException("该聊天下存在重复的同名任务")
    } else throw ExecutionException("该聊天下存在重复的同名任务")

  }
}

object CronTaskPool {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(CronTaskPool.getClass)

  // 用于定时任务的转换操作
  implicit val cronTaskWaitingPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def apply(): CronTaskPool = {
    new CronTaskPool()
  }
}
