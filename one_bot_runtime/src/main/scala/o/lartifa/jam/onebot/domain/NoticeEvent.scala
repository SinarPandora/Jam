package o.lartifa.jam.onebot.domain

/**
 * 业务侧通知事件。
 * 热路径：poke / 群员增减；其余为 UnhandledNotice（本阶段 no-op，SSDL 接通见 #44）。
 *
 * Author: sinar
 * 2026/11/28
 */
sealed trait NoticeEvent {
  def postType: String
  def noticeType: String
  def selfId: Long
  def userId: Long
  def time: Long
  def subType: Option[String]

  /** 多账号去重用的内容指纹（不含 time）。 */
  def contentKey: String
}

final case class FriendPokeNotice(
  postType: String,
  noticeType: String,
  selfId: Long,
  userId: Long,
  time: Long,
  subType: Option[String],
  targetId: Long,
) extends NoticeEvent {
  def contentKey: String = s"friend_poke|$userId|$targetId|$noticeType"
}

final case class GroupPokeNotice(
  postType: String,
  noticeType: String,
  selfId: Long,
  userId: Long,
  time: Long,
  subType: Option[String],
  groupId: Long,
  targetId: Long,
) extends NoticeEvent {
  def contentKey: String = s"group_poke|$groupId|$userId|$targetId|$noticeType"
}

final case class GroupMemberIncreaseNotice(
  postType: String,
  noticeType: String,
  selfId: Long,
  userId: Long,
  time: Long,
  subType: Option[String],
  groupId: Long,
  operatorId: Long,
) extends NoticeEvent {
  def contentKey: String = s"group_inc|$groupId|$userId|$operatorId|${subType.getOrElse("")}"
}

final case class GroupMemberDecreaseNotice(
  postType: String,
  noticeType: String,
  selfId: Long,
  userId: Long,
  time: Long,
  subType: Option[String],
  groupId: Long,
  operatorId: Long,
) extends NoticeEvent {
  def contentKey: String = s"group_dec|$groupId|$userId|$operatorId|${subType.getOrElse("")}"
}

/** 已识别为 notice、但本阶段不进入业务监听的冷路径。 */
final case class UnhandledNotice(
  postType: String,
  noticeType: String,
  selfId: Long,
  userId: Long,
  time: Long,
  subType: Option[String],
  groupId: Option[Long],
) extends NoticeEvent {
  def contentKey: String =
    s"unhandled|$noticeType|${groupId.getOrElse(0L)}|$userId|${subType.getOrElse("")}"
}
