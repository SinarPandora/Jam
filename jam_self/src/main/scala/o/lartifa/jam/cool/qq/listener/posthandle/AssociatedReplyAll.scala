package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.sender.message.components.ComponentAt
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.{CommandExecuteContext, VarKey}
import o.lartifa.jam.plugins.caiyunai.dream.DreamFastClient
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 在任何情况下进行联想回复
 *
 * Author: sinar
 * 2021/6/13 03:53
 */
object AssociatedReplyAll extends PostHandleTask("联想回复（匹配后）", TriBoolValue.Both) {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)
  private val atMe: String = new ComponentAt(JamConfig.qID).toString
  private val varModelId: VarKey = VarKey("彩云小梦默认AI编号", VarKey.DB)

  /**
   * 执行
   *
   * @param event      消息对象
   * @param contextOpt 执行上下文
   * @param exec       异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage, contextOpt: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] = Future {
    implicit val context: CommandExecuteContext = contextOpt.getOrElse(CommandExecuteContext(event))
    val msg = event.message.replace(" ", "")
    if (msg.contains(atMe)) {
      varModelId.query.map(_.map(_.toInt).getOrElse(0)).flatMap(modelId =>
        DreamFastClient.reply(content = msg.replace(atMe, ""), modelId)
      ).map {
        case Some(result) =>
          val resp = result.trim.replaceFirst("[,，。！.!？?;；\n\t]+", "")
          resp.sliding(200, 200).foreach(event.respond)
        case None => event.respond("🤔")
      }.recover(err => {
        event.respond("😴")
        logger.error(err)
      })
    }
  }
}
