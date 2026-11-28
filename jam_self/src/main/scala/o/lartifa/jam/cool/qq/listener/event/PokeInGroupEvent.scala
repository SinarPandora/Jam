package o.lartifa.jam.cool.qq.listener.event

import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.ContentMatcher.Events
import o.lartifa.jam.onebot.domain.GroupPokeNotice

import java.sql.Timestamp

/**
 * 群聊双击头像事件
 *
 * Author: sinar
 * 2021/7/9 22:25
 */
case class PokeInGroupEvent(event: GroupPokeNotice) extends CQEvent(Events.PokeInGroup.name, event.userId) {
  override val chatInfo: ChatInfo = ChatInfo(MessageType.GROUP, event.groupId)

  override val willNotResponse: Boolean = event.userId == BotConfig.qID

  override val data: Map[String, String] = Map(
    "目标QQ" -> event.targetId.toString,
    "目标群昵称" -> EventEnrichment.groupCardOrNick(event.groupId, event.targetId),
    "发送者QQ" -> event.userId.toString,
    "发送者群昵称" -> EventEnrichment.groupCardOrNick(event.groupId, event.userId),
    "群号" -> event.groupId.toString,
    "群名" -> EventEnrichment.groupName(event.groupId),
    "事件类型" -> event.postType,
    "通知类型" -> event.noticeType,
    "事件子类型" -> event.subType.getOrElse(""),
    "会话ID" -> event.groupId.toString,
    "发送时间" -> new Timestamp(event.time * 100).toString,
  )
}
