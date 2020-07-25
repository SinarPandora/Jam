package o.lartifa.jam.model.tasks

import java.util.concurrent.atomic.AtomicBoolean

import cc.moecraft.logger.HyLogger
import cn.hutool.core.lang.UUID
import cn.hutool.cron.CronUtil
import cn.hutool.cron.task.Task
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.tasks.JamCronTask.logger
import o.lartifa.jam.pool.JamContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 定时任务封装
 *
 * Author: sinar
 * 2020/1/25 13:45
 */
abstract class JamCronTask(val name: String, val chatInfo: ChatInfo = ChatInfo.None, val id: UUID = UUID.randomUUID()) extends Task {

  import o.lartifa.jam.pool.CronTaskPool.cronTaskWaitingPool

  val idString: String = id.toString(true)

  val isRunning: AtomicBoolean = new AtomicBoolean(false)


  /**
   * 修改默认的执行模式
   */
  override final def execute(): Unit = {
    if (isRunning.get()) {
      logger.log(s"任务：${name}正在运行，本次触发被跳过")
    } else {
      logger.log(s"任务执行开始：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")
      Try(Await.result(run(), Duration.Inf)) match {
        case Failure(exception) =>
          exception match {
            case e: InterruptedException => logger.error(s"任务执行被打断：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id", e)
            case e => logger.error(s"任务执行执行出错：错误信息：${e.getMessage}，定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id", e)
          }
        case Success(_) => logger.log(s"任务执行结束：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")
      }
      isRunning.getAndSet(false)
    }
  }

  /**
   * 执行定时任务内容
   *
   * @return 并发占位符
   */
  def run()(implicit exec: ExecutionContext): Future[Unit]

  /**
   * 设置并启动定时任务
   *
   * @param cron 定时表达式
   */
  def setUp(cron: String): Unit = if (!isRunning.get()) {
    CronUtil.schedule(idString, cron, this)
    JamContext.cronTaskPool.get().add(this)
    isRunning.getAndSet(true)
  } else throw ExecutionException(s"该任务已经被初始化过了并正在运行：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")

  /**
   * 取消该定时任务
   */
  def cancel(): Unit = {
    CronUtil.remove(idString)
    JamContext.cronTaskPool.get().remove(this)
    isRunning.getAndSet(false)
  }
}

object JamCronTask {
  val logger: HyLogger = JamContext.logger.get()

  case class TaskDefinition[T <: JamCronTask](name: String, cls: Class[T], isSingleton: Boolean)

}
