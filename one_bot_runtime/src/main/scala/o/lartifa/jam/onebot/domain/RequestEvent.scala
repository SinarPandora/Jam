package o.lartifa.jam.onebot.domain

import o.lartifa.jam.onebot.protocol.api.OutboundGateway

/**
 * 业务侧请求事件（热路径：好友申请 / 群邀请）。
 *
 * Author: sinar
 * 2026/11/28
 */
sealed trait RequestEvent {
  def postType: String
  def requestType: String
  def flag: String
  def userId: Long
  def selfId: Long
  def time: Long
  def comment: String

  def accept(): Unit
  def reject(reason: String = ""): Unit
}

final case class FriendAddRequest(
  postType: String,
  requestType: String,
  flag: String,
  userId: Long,
  selfId: Long,
  time: Long,
  comment: String,
  outbound: OutboundGateway,
) extends RequestEvent {
  def accept(): Unit = outbound.setFriendAddRequest(flag, approve = true)
  def reject(reason: String = ""): Unit = outbound.setFriendAddRequest(flag, approve = false)
}

final case class GroupInviteRequest(
  postType: String,
  requestType: String,
  flag: String,
  userId: Long,
  selfId: Long,
  time: Long,
  comment: String,
  groupId: Long,
  subType: String,
  outbound: OutboundGateway,
) extends RequestEvent {
  def accept(): Unit = outbound.setGroupAddRequest(flag, subType, approve = true)
  def reject(reason: String = ""): Unit =
    outbound.setGroupAddRequest(flag, subType, approve = false, reason = reason)
}
