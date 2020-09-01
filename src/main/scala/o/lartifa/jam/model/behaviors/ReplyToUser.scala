package o.lartifa.jam.model.behaviors

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventPrivateMessage}
import cc.moecraft.icq.sender.message.components.ComponentAt
import o.lartifa.jam.model.CommandExecuteContext

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/9/2 00:56
 */
trait ReplyToUser {
  /**
   * 消息中对方的昵称
   * 群：尝试获取群昵称，不存在/失败则使用 QQ 昵称
   * 私聊：QQ 昵称
   *
   * @param context 指令上下文
   * @param exec    执行上下文
   * @return 昵称
   */
  protected def senderNickName(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = async {
    context.eventMessage match {
      case _: EventGroupOrDiscussMessage =>
        await(context.tempVars.get("对方群昵称")) match {
          case Some(value) => value
          case None => await(context.tempVars.get("对方昵称")).getOrElse("这个人")
        }
      case _: EventPrivateMessage =>
        await(context.tempVars.get("对方昵称")).getOrElse("你")
      case _ => "这个人"
    }
  }

  /**
   * At 消息发送者
   *
   * @param context 指令上下文
   * @return at 的 CQ 码
   */
  protected def atSender(implicit context: CommandExecuteContext): String =
    new ComponentAt(context.eventMessage.getSenderId).toString
}
