package o.lartifa.jam.cool.qq.listener

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.event.{EventHandler, IcqListener}
import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.core.date.StopWatch
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

/**
 * 全局消息解析器
 *
 * Author: sinar
 * 2020/1/2 22:20 
 */
object RuleEngineListener extends IcqListener {

  private implicit val exec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  private lazy val logger: HyLogger = JamContext.logger.get()

  @EventHandler
  def listen(eventMessage: EventMessage): Unit = {
    val matchCost = new StopWatch()
    matchCost.start()
    val stepId: AtomicReference[Option[Long]] = new AtomicReference[Option[Long]](None)
    if (!JamContext.editLock.get()) {
      import scala.util.control._
      val loop = new Breaks
      loop.breakable {
        for (matcher <- JamContext.matchers.get()) {
          if (matcher.isMatched(eventMessage.message)) {
            implicit val context: CommandExecuteContext = CommandExecuteContext(eventMessage)
            stepId.set(Some(matcher.stepId))
            JamContext.stepPool.get().goto(matcher.stepId).onComplete {
              case Failure(exception) =>
                exception.printStackTrace()
                notifyMaster(matcher.stepId, exception.getMessage, eventMessage)
              case Success(_) =>
            }
            loop.break()
          }
        }
      }
    }
    matchCost.stop()
    stepId.get().foreach { id =>
      val cost = matchCost.getTotalTimeSeconds
      if (cost < 1) {
        logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$id，耗时：小于1s")
      } else if (cost < 4) {
        logger.log(s"${AnsiColor.GREEN}成功捕获！步骤ID：$id，耗时：${cost}s")
      } else {
        logger.warning(s"${AnsiColor.RED}成功捕获但耗时较长，请考虑对步骤进行优化。步骤ID：$id，耗时：${cost}s")
      }
    }
  }

  /**
   * 通知管理者
   *
   * @param stepId       步骤 ID
   * @param message      错误信息
   * @param eventMessage 消息内容
   */
  private def notifyMaster(stepId: Long, message: String, eventMessage: EventMessage): Unit = {
    eventMessage.getHttpApi.sendPrivateMsg(JamConfig.masterQID, s"步骤${stepId}执行失败！原因：$message")
  }
}
