package o.lartifa.jam.model.commands

import cc.moecraft.icq.sender.message.components.ComponentContact
import cc.moecraft.icq.sender.message.components.ComponentContact.ContactType
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.model.CommandExecuteContext

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 分享聊天
 *
 * Author: sinar
 * 2020/12/27 02:03
 */
case class ShareContact(qId: RenderStrTemplate, contactType: ContactType) extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = {
    qId.execute().map(qId =>
      reply(new ComponentContact(Try(qId.toLong)
        .getOrElse(throw ExecutionException(s"分享的聊天ID不合法：$qId")), contactType)))
  }
}

object ShareContact {

  sealed abstract class ShareType(val str: String)

  case object Group extends ShareType("群聊")

  case object Friend extends ShareType("好友")

}
