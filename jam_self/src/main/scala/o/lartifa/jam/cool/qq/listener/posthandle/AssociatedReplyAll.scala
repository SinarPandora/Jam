package o.lartifa.jam.cool.qq.listener.posthandle

import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.sender.message.components.ComponentAt
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.TriBoolValue
import o.lartifa.jam.model.commands.DreamingReply
import o.lartifa.jam.model.{CommandExecuteContext, VarKey}

import scala.concurrent.{ExecutionContext, Future}

/**
 * 在任何情况下进行联想回复
 *
 * Author: sinar
 * 2021/6/13 03:53
 */
object AssociatedReplyAll extends PostHandleTask("联想回复（匹配后）", TriBoolValue.Both) {
  private val atMe: String = new ComponentAt(BotConfig.qID).toString

  /**
   * 执行
   *
   * @param event      消息对象
   * @param contextOpt 执行上下文
   * @param exec       异步上下文
   * @return 异步返回执行结果
   */
  override def execute(event: EventMessage, contextOpt: Option[CommandExecuteContext])(implicit exec: ExecutionContext): Future[Unit] = {
    implicit val context: CommandExecuteContext = contextOpt.getOrElse(CommandExecuteContext(event))
    val msg = event.message.replaceAll("\\s+", "")
    if (msg.startsWith(atMe)) {
      DreamingReply(VarKey("$TEMP", VarKey.Mocked, Some(msg.replace(atMe, "")))).execute()
    } else {
      Future.unit
    }
  }
}
