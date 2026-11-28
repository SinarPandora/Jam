package o.lartifa.jam.cool.qq.listener.event

import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.ContentMatcher.Events
import o.lartifa.jam.onebot.domain.GroupMemberDecreaseNotice

import java.sql.Timestamp

/**
 * 群员减少事件
 *
 * Author: sinar
 * 2021/7/9 22:27
 */
case class MemberDecEvent(event: GroupMemberDecreaseNotice)
  extends CQEvent(Events.MemberDec.name, event.operatorId) {
  override val chatInfo: ChatInfo = ChatInfo(MessageType.GROUP, event.groupId)

  override val willNotResponse: Boolean = event.userId == BotConfig.qID

  override val data: Map[String, String] = Map(
    "群ID" -> event.groupId.toString,
    "群QQ" -> event.groupId.toString,
    "群名" -> EventEnrichment.groupName(event.groupId),
    "群员QQ" -> event.userId.toString,
    "操作者QQ" -> event.operatorId.toString,
    "事件类型" -> event.postType,
    "通知类型" -> event.noticeType,
    "事件子类型" -> event.subType.getOrElse(""),
    "会话ID" -> event.groupId.toString,
    "发送时间" -> new Timestamp(event.time * 100).toString,
  )
}
