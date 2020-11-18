package o.lartifa.jam.pool

import java.util.concurrent.Executors

import cc.moecraft.logger.HyLogger
import cn.hutool.cron.CronUtil
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.tasks.{ChangeRespFrequency, GoASleep, JamCronTask, WakeUp}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.plugins.picbot.FetchPictureTask
import o.lartifa.jam.pool.CronTaskPool.{TaskDefinition, logger}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

/**
 * 定时任务池
 *
 * Author: sinar
 * 2020/1/25 13:32
 */
class CronTaskPool {

  private var _taskDefinition: Map[String, TaskDefinition] = Map.empty
  private val runningTasks: mutable.Map[String, ListBuffer[JamCronTask]] = mutable.Map.empty
  private var initing: Boolean = false

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
    initing = true
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
    this._taskDefinition.values.foreach(_.startRequireTasks(this))
    // 如果是单例任务并且尚未被自动初始化，必须强制在此初始化一遍（确保其为单例）
    _taskDefinition.filter(it => !runningTasks.keySet.contains(it._1) && it._2.isSingleton).foreach {
      case(name, definition) => runningTasks += name -> ListBuffer(definition.init(this))
    }
    logger.log("定时任务定义刷新完成")
    initing = false
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
  private def getAll(name: String): List[JamCronTask] = {
    if (initing) {
      runningTasks.getOrElse(name, ListBuffer.empty).toList
    } else throw ExecutionException("该方法不能在定时任务初始化之外被调用")
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
   * 通过任务名获取唯全部已经激活的定时任务
   *
   * @param name    任务名
   * @param context 任务执行上下文（可选）
   * @throws ExecutionException 执行异常，当定时任务不唯一时抛出
   * @return 定时任务（若任务为单例，返回 left，否则返回 right 并包含全部已存在的 task）
   */
  @throws[ExecutionException]
  def getActiveTasks(name: String, inChat: Boolean = false)(implicit context: CommandExecuteContext = null): Either[JamCronTask, List[JamCronTask]] = {
    val define = _taskDefinition.getOrElse(name, throw ExecutionException(s"任务不存在！$name"))
    val searchPath = this.getAll(name)
    if (define.isSingleton) {
      if (searchPath.lengthIs == 1) Left(searchPath.head)
      else if (searchPath.lengthIs == 0) throw ExecutionException(s"单例任务${name}尚未初始化")
      else throw ExecutionException(s"存在重复的单例任务$name")
    } else {
      // 找到全部满足条件的 task
      val path = if (inChat) {
        if (context != null) {
          val chatInfo = context.chatInfo
          searchPath.filter(_.chatInfo == chatInfo)
        } else throw ExecutionException("聊天上下文不存在！")
      } else searchPath
      Right(path)
    }
  }
}

object CronTaskPool {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(CronTaskPool.getClass)

  // 用于定时任务的转换操作
  implicit val cronTaskWaitingPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def apply(): CronTaskPool = new CronTaskPool()

  case class CronPair(cron: String)

  case class TaskDefinition
  (
    /**
     * 任务名
     */
    name: String,

    /**
     * 定义类
     */
    cls: Class[_ <: JamCronTask],

    /**
     * 是否是单例任务
     */
    isSingleton: Boolean,

    /**
     * 在初始化后立刻创建按照如下 cron 定义的任务
     * 注意：定义了该变量的任务必须与聊天会话无关
     */
    createTasksAfterInit: List[CronPair] = Nil
  ) {
    /**
     * 根据 { createTasksAfterInit } 初始化对应数量和时间的任务
     *
     * @param pool 定时任务池
     */
    def startRequireTasks(pool: CronTaskPool): Unit = {
      if (createTasksAfterInit.nonEmpty) {
        createTasksAfterInit.map(_.cron).foreach(init(pool).setUp)
      }
    }

    /**
     * 初始化
     *
     * @param pool 任务池
     * @return 实例
     */
    def init(pool: CronTaskPool = JamContext.cronTaskPool.get()): JamCronTask = {
      if (!isSingleton || pool.getAll(name).isEmpty) {
        cls.getDeclaredConstructor(classOf[String]).newInstance(name)
      } else throw ExecutionException(s"单例任务 '$name' 不能被初始化多次！")
    }
  }
}
