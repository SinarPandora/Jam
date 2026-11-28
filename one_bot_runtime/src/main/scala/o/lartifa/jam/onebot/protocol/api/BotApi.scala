package o.lartifa.jam.onebot.protocol.api

/**
 * 领域出站 Bot API（#38）：消息 + 群管 + 查询；不含 cookies/csrf/restart/录音。
 * Lambda `$api` 与 `JamContext.apiClient` 均绑此表面。
 *
 * Author: sinar
 * 2026/11/28
 */
trait BotApi {

  // --- 消息 ---
  def sendPrivateMsg(userId: Long, message: String, autoEscape: Boolean = false): Long
  def sendGroupMsg(groupId: Long, message: String, autoEscape: Boolean = false): Long
  def sendDiscussMsg(discussId: Long, message: String, autoEscape: Boolean = false): Long

  // --- 群管 ---
  def setGroupBan(groupId: Long, userId: Long, durationSeconds: Long): Unit
  def setGroupWholeBan(groupId: Long, enable: Boolean): Unit
  def setGroupKick(groupId: Long, userId: Long, rejectFurtherRequest: Boolean = false): Unit
  def setGroupCard(groupId: Long, userId: Long, card: String): Unit
  def setGroupAdmin(groupId: Long, userId: Long, enable: Boolean): Unit
  def setGroupLeave(groupId: Long, dismiss: Boolean = false): Unit
  def setGroupSpecialTitle(groupId: Long, userId: Long, specialTitle: String, duration: Long = -1L): Unit
  def setDiscussLeave(discussId: Long): Unit

  // --- 请求（与 OutboundGateway 对齐，供脚本） ---
  def setFriendAddRequest(flag: String, approve: Boolean, remark: String = ""): Unit
  def setGroupAddRequest(flag: String, requestType: String, approve: Boolean, reason: String = ""): Unit

  // --- 查询 ---
  def getLoginInfo(): Option[LoginInfo]
  def getStrangerInfo(userId: Long, noCache: Boolean = false): Option[StrangerInfo]
  def getGroupInfo(groupId: Long, noCache: Boolean = false): Option[GroupInfo]
  def getGroupMemberInfo(groupId: Long, userId: Long, noCache: Boolean = false): Option[GroupMemberInfo]
  def getGroupMemberList(groupId: Long): List[GroupMemberInfo]
  def getFriendList(): List[FriendInfo]
  def getGroupList(): List[GroupSummary]
}

final case class LoginInfo(userId: Long, nickname: String)

final case class StrangerInfo(
  userId: Long,
  nickname: String,
  sex: String = "unknown",
  age: Int = 0,
)

final case class GroupInfo(groupId: Long, groupName: String)

final case class GroupMemberInfo(
  userId: Long,
  nickname: String,
  card: String = "",
  sex: String = "unknown",
  age: Int = 0,
  role: String = "member",
  unfriendly: Boolean = false,
  groupId: Long = 0L,
)

final case class FriendInfo(userId: Long, nickname: String = "", remark: String = "")

final case class GroupSummary(groupId: Long, groupName: String = "")
