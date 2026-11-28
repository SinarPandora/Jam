package o.lartifa.jam.onebot.domain

import o.lartifa.jam.onebot.protocol.api.OutboundGateway

/**
 * 业务侧消息领域模型（与 OneBot wire / legacy EventMessage 解耦）。
 *
 * Author: sinar
 * 2026/11/27
 */
final case class SenderInfo(
  userId: Long,
  nickname: Option[String] = None,
  card: Option[String] = None,
  role: Option[String] = None,
  age: Option[Int] = None,
  sex: Option[String] = None,
) {
  /** 群名片优先，否则 QQ 昵称，再否则 QQ 号。 */
  def displayName: String =
    card.filter(_.nonEmpty).orElse(nickname.filter(_.nonEmpty)).getOrElse(userId.toString)

  def isAdmin: Boolean =
    role.exists(r => r.equalsIgnoreCase("admin") || r.equalsIgnoreCase("owner"))
}

sealed trait ChatMessage {
  def postType: String
  def messageType: String
  def messageId: Long
  def senderId: Long
  def selfId: Long
  def time: Long
  def message: String
  def rawMessage: String
  def font: Long
  def subType: Option[String]
  def sender: SenderInfo
  def outbound: OutboundGateway

  /** 回复当前会话（群/讨论组/私聊）。返回 message_id，失败为 -1。 */
  def reply(msg: String, autoEscape: Boolean = false): Long

  /** 强制私聊回复发送者。 */
  def replyPrivate(msg: String, autoEscape: Boolean = false): Long =
    outbound.sendPrivate(senderId, msg, autoEscape)

  /** 兼容 legacy `respond` 命名。 */
  def respond(msg: String, autoEscape: Boolean = false): Long = reply(msg, autoEscape)
}

final case class PrivateMessage(
  postType: String,
  messageType: String,
  messageId: Long,
  senderId: Long,
  selfId: Long,
  time: Long,
  message: String,
  rawMessage: String,
  font: Long,
  subType: Option[String],
  sender: SenderInfo,
  outbound: OutboundGateway,
) extends ChatMessage {
  def reply(msg: String, autoEscape: Boolean = false): Long =
    outbound.sendPrivate(senderId, msg, autoEscape)
}

final case class GroupMessage(
  postType: String,
  messageType: String,
  messageId: Long,
  senderId: Long,
  selfId: Long,
  time: Long,
  message: String,
  rawMessage: String,
  font: Long,
  subType: Option[String],
  groupId: Long,
  sender: SenderInfo,
  outbound: OutboundGateway,
) extends ChatMessage {
  def reply(msg: String, autoEscape: Boolean = false): Long =
    outbound.sendGroup(groupId, msg, autoEscape)
}

final case class DiscussMessage(
  postType: String,
  messageType: String,
  messageId: Long,
  senderId: Long,
  selfId: Long,
  time: Long,
  message: String,
  rawMessage: String,
  font: Long,
  subType: Option[String],
  discussId: Long,
  sender: SenderInfo,
  outbound: OutboundGateway,
) extends ChatMessage {
  def reply(msg: String, autoEscape: Boolean = false): Long =
    outbound.sendDiscuss(discussId, msg, autoEscape)
}
