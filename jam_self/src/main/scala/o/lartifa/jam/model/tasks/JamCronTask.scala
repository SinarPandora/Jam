package o.lartifa.jam.model.tasks

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

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}

/**
 * 定时任务封装
 *
 * Author: sinar
 * 2020/1/25 13:45
 */
abstract class JamCronTask(val name: String, val chatInfo: ChatInfo = ChatInfo.None, val id: String = UUID.randomUUID().toString(),
                           val onlyOnce: Boolean = false) extends Task {

  import o.lartifa.jam.pool.CronTaskPool.cronTaskWaitingPool

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
      run()
        .recover {
          case e: InterruptedException => MasterUtil.notifyAndLog(s"%s，任务执行被打断：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id",
            LogLevel.ERROR, Some(e))
          case e =>
            MasterUtil.notifyAndLog(s"任务执行出错：错误信息：${e.getMessage}，定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id",
              LogLevel.ERROR, Some(e))
        }
        .foreach { _ =>
          logger.log(s"任务执行结束：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")
          stopWatch.stop()
          val cost = stopWatch.getTotalTimeSeconds
          if (cost < 1) logger.log(s"${AnsiColor.GREEN}任务 ID：$id 执行耗时：小于1s")
          else logger.log(s"${AnsiColor.GREEN}}任务 ID：$id 执行耗时：${cost}s")
          postRun()
          isRunning.getAndSet(false)
        }
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
   * 默认判断任务若只需执行一次，则在执行后自动取消任务
   */
  def postRun(): Unit = if (onlyOnce) this.cancel()

  /**
   * 设置并启动定时任务
   *
   * @param cron 定时表达式
   */
  @throws[ExecutionException]
  def setUp(cron: String): Unit = if (!isRunning.get()) {
    CronUtil.schedule(id, cron, this)
    JamContext.cronTaskPool.get().add(this)
  } else throw ExecutionException(s"该任务已经被初始化过了并正在运行：定时任务名称：$name，聊天信息：$chatInfo，唯一 ID：$id")

  /**
   * 取消该定时任务
   *
   * @param removeFromTaskPool 是否从定时任务池中删除（默认是）
   */
  def cancel(removeFromTaskPool: Boolean = true): Unit = {
    CronUtil.remove(id)
    if (removeFromTaskPool) JamContext.cronTaskPool.get().remove(this)
    isRunning.getAndSet(false)
  }
}

object JamCronTask {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(JamCronTask.getClass)
}
