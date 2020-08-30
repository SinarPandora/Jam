package o.lartifa.jam.model.tasks

import java.util.concurrent.atomic.AtomicBoolean

import cc.moecraft.logger.format.AnsiColor
import cc.moecraft.logger.{HyLogger, LogLevel}
import cn.hutool.core.date.StopWatch
import cn.hutool.core.lang.UUID
import cn.hutool.cron.CronUtil
import cn.hutool.cron.task.Task
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.MasterUtil
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
      isRunning.getAndSet(true)
      val stopWatch = new StopWatch()
      stopWatch.start()
      logger.log(s"任务执行开始：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")
      Try(Await.result(run(), Duration.Inf)) match {
        case Failure(exception) =>
          exception match {
            case e: InterruptedException => MasterUtil.notifyAndLog(s"任务执行被打断：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id",
              LogLevel.ERROR, Some(e))
            case e =>
              MasterUtil.notifyAndLog(s"任务执行执行出错：错误信息：${e.getMessage}，定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id",
                LogLevel.ERROR, Some(e))
          }
        case Success(_) => logger.log(s"任务执行结束：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")
      }
      stopWatch.stop()
      val cost = stopWatch.getTotalTimeSeconds
      if (cost < 1) logger.log(s"${AnsiColor.GREEN}任务 ID：$id 执行耗时：小于1s")
      else logger.log(s"${AnsiColor.GREEN}}任务 ID：$id 执行耗时：${cost}s")
      postRun()
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
   * 完成后执行
   */
  def postRun(): Unit = {}

  /**
   * 设置并启动定时任务
   *
   * @param cron 定时表达式
   */
  def setUp(cron: String): Unit = if (!isRunning.get()) {
    CronUtil.schedule(idString, cron, this)
    JamContext.cronTaskPool.get().add(this)
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
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(JamCronTask.getClass)

  case class TaskDefinition(name: String, cls: Class[_ <: JamCronTask], isSingleton: Boolean)

}
