package o.lartifa.jam.model.behaviors

import o.lartifa.jam.ob.sender.returndata.returnpojo.get.RGroupMemberInfo
import o.lartifa.jam.ob.sender.returndata.ReturnStatus
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.onebot.cq.Cq
import o.lartifa.jam.onebot.domain.{DiscussMessage, GroupMessage, PrivateMessage}
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import scala.language.implicitConversions

/**
 * 行为：回复给朋友
 *
 * Author: sinar
 * 2020/9/2 00:56
 */
trait ReplyToFriend {

  /**
   * 是否为私聊
   *
   * @param context 指令上下文
   * @return 是否为私聊
   */
  protected def isPrivate(implicit context: CommandExecuteContext): Boolean = context.eventMessage.isInstanceOf[PrivateMessage]

  /**
   * 是否为群聊
   *
   * @param context 指令上下文
   * @return 是否为群聊
   */
  protected def isGroup(implicit context: CommandExecuteContext): Boolean = context.eventMessage.isInstanceOf[GroupMessage] || context.eventMessage.isInstanceOf[DiscussMessage]

  /**
   * 获取当前消息的发送者 QQ
   *
   * @param context 指令上下文
   * @return 发送者 QQ
   */
  protected def senderId(implicit context: CommandExecuteContext): Long = context.eventMessage.senderId

  /**
   * 获取指定群聊的群员列表
   *
   * @param groupId 群号
   * @param exec    异步上下文
   * @return 群员列表
   */
  protected def groupMembers(groupId: Long)(implicit exec: ExecutionContext): Future[Option[List[RGroupMemberInfo]]] = Future {
    val result = JamContext.apiClient.getGroupMemberList(groupId)
    if (result.status == ReturnStatus.ok) Some(result.data.asScala.toList)
    else None
  }

  /**
   * 获取当前会话中的群员列表
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 群员列表
   */
  protected def groupMembers(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Option[List[RGroupMemberInfo]]] =
    context.eventMessage match {
      case message: GroupMessage => groupMembers(message.groupId)
      case message: DiscussMessage => groupMembers(message.discussId)
      case _: PrivateMessage => Future.successful(None)
    }

  /**
   * 消息中对方的昵称
   * 群：尝试获取群昵称，不存在/失败则使用 QQ 昵称
   * 私聊：QQ 昵称
   *
   * @param context 指令上下文
   * @param exec    执行上下文
   * @return 昵称
   */
  protected def senderNickName(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] =
    context.eventMessage match {
      case _: GroupMessage | _: DiscussMessage =>
        context.tempVars.get("对方群昵称").flatMap {
          case Some(value) => Future.successful(value)
          case None => context.tempVars.get("对方昵称").map(_.getOrElse("这个人"))
        }
      case _: PrivateMessage =>
        context.tempVars.get("对方昵称").map(_.getOrElse("你"))
      case _ => Future.successful("这个人")
    }

  /**
   * At 消息发送者
   *
   * @param context 指令上下文
   * @return at 的 CQ 码
   */
  protected def atSender(implicit context: CommandExecuteContext): String =
    Cq.At(context.eventMessage.senderId).toCq

  /**
   * 快速回复消息
   *
   * @param anyThing 任何对象
   * @param context  指令上下文
   * @return 操作结果
   */
  protected def reply(anyThing: Any)(implicit context: CommandExecuteContext): Long =
    context.eventMessage.respond(anyThing.toString)

  /**
   * 快速回复消息（不转义）
   *
   * @param anyThing 任何对象
   * @param context  指令上下文
   * @return 操作结果
   */
  protected def replyRaw(anyThing: Any)(implicit context: CommandExecuteContext): Long =
    context.eventMessage.respond(anyThing.toString, false)
}
