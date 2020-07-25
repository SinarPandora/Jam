package o.lartifa.jam.pool

import java.util.concurrent.Executors

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.tasks.JamCronTask.TaskDefinition
import o.lartifa.jam.model.tasks.{ChangeRespFrequency, GoASleep, JamCronTask, WakeUp}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

/**
 * 定时任务池
 *
 * Author: sinar
 * 2020/1/25 13:32
 */
class CronTaskPool(val taskDefinition: Map[String, TaskDefinition]) {

  private val runningTasks: mutable.Map[String, ListBuffer[JamCronTask]] = mutable.Map.empty
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
  // 用于定时任务的转换操作
  implicit val cronTaskWaitingPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def apply(): CronTaskPool = {
    val taskDefinition = Map(
      "回复频率变更" -> TaskDefinition("", classOf[ChangeRespFrequency], isSingleton = false),
      "睡眠" -> TaskDefinition("", classOf[GoASleep], isSingleton = true),
      "起床" -> TaskDefinition("", classOf[WakeUp], isSingleton = true),
    )
    new CronTaskPool(taskDefinition)
  }
}
