package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.sender.message.components.ComponentAt
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.plugins.caiyunai.dream.DreamFastClient
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 后置任务：联想回复
 * 若消息内容包含艾特 bot，并且没有被捕获，则联想消息内容并给出回复
 *
 * Author: sinar
 * 2021/6/12 21:29
 */
object AssociatedReply extends PostUnProcessedHandleTask("联想回复") {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)
  private val atMe: String = new ComponentAt(JamConfig.qID).toString

  /**
   * 执行
   *
   * @param event 消息对象
   * @param exec  异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage)(implicit exec: ExecutionContext): Future[Unit] = {
    val msg = event.message.replace(" ", "")
    if (msg.contains(atMe)) {
      DreamFastClient.reply(content = msg.replace(atMe, "")).map {
        case Some(response) =>
          event.respond(response)
        case None => event.respond("🤔")
      }.recover(err => {
        event.respond("😴")
        logger.error(err)
      })
    }
  }
}
