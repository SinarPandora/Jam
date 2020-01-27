package o.lartifa.jam.pool

import cn.hutool.core.lang.UUID
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.tasks.{CronTaskWithInfo, JamCronTask}
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import org.reflections.Reflections

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

/**
 * 定时任务池
 *
 * Author: sinar
 * 2020/1/25 13:32
 */
class CronTaskPool(private val tasks: mutable.Map[String, ListBuffer[CronTaskWithInfo]]) {
  /**
   * 添加定时任务到定时任务池
   *
   * @param taskWithInfo 任务信息组
   */
  def add(taskWithInfo: CronTaskWithInfo): Unit = {
    tasks.getOrElseUpdate(taskWithInfo.task.name, ListBuffer.empty).addOne(taskWithInfo)
  }

  /**
   * 添加全部任务到定时任务池
   *
   * @param tasksWithInfo 任务信息组列表
   */
  def addAll(tasksWithInfo: Seq[CronTaskWithInfo]): Unit = {
    tasksWithInfo.groupBy(_.task.name).foreach { case (name, tasksWithSameName) =>
      tasks.getOrElseUpdate(name, ListBuffer.empty).addAll(tasksWithSameName)
    }
  }

  /**
   * 获取全部同名定时任务
   *
   * @param name 任务名
   * @return 任务列表
   */
  def getAll(name: String): List[CronTaskWithInfo] = {
    tasks.getOrElse(name, ListBuffer.empty).filter(_.task.name == name).toList
  }

  /**
   * 获取当前会话下的全部同名任务
   *
   * @param name     任务名
   * @param chatInfo 会话信息
   * @return 任务列表
   */
  def getAll(name: String, chatInfo: ChatInfo): List[CronTaskWithInfo] = {
    getAll(name).filter(_.chatInfo.contains(chatInfo))
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
  def get(name: String)(implicit context: CommandExecuteContext = null): Option[CronTaskWithInfo] = {
    val searchPath = this.getAll(name)
    if (searchPath.lengthIs == 0) {
      None
    } else if (searchPath.lengthIs == 1) {
      searchPath.headOption
    } else if (context != null) {
      val chatInfo = context.chatInfo
      val tasks = searchPath.filter(_.chatInfo.contains(chatInfo))
      if (tasks.lengthIs == 1) tasks.headOption
      else throw ExecutionException("该聊天下存在重复的同名任务")
    } else throw ExecutionException("该聊天下存在重复的同名任务")

  }
}

object CronTaskPool {
  def apply(): CronTaskPool = {
    val mutMap: mutable.Map[String, ListBuffer[CronTaskWithInfo]] = mutable.Map.empty
    new Reflections("o.lartifa.jam.model.tasks")
      .getSubTypesOf(classOf[JamCronTask])
      .asScala
      .foreach(taskClz => {
        val task = taskClz.getDeclaredConstructor().newInstance()
        mutMap.getOrElseUpdate(task.name, ListBuffer.empty).addOne(CronTaskWithInfo(task, None, UUID.randomUUID()))
      })
    new CronTaskPool(mutMap)
  }
}
