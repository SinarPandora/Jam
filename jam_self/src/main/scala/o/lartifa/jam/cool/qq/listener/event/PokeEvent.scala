package o.lartifa.jam.cool.qq.listener.event

import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.ContentMatcher.Events
import o.lartifa.jam.onebot.domain.FriendPokeNotice

import java.sql.Timestamp

/**
 * 私聊双击头像
 *
 * Author: sinar
 * 2021/7/6 22:29
 */
case class PokeEvent(event: FriendPokeNotice) extends CQEvent(Events.Poke.name, event.userId) {
  override val chatInfo: ChatInfo = ChatInfo(MessageType.PRIVATE, event.userId)

  override val willNotResponse: Boolean = event.userId == BotConfig.qID

  override val data: Map[String, String] = Map(
    "自己QQ" -> event.selfId.toString,
    "自己昵称" -> EventEnrichment.botName,
    "发送者QQ" -> event.userId.toString,
    "发送者昵称" -> EventEnrichment.strangerNickname(event.userId),
    "事件类型" -> event.postType,
    "通知类型" -> event.noticeType,
    "事件子类型" -> event.subType.getOrElse(""),
    "会话ID" -> event.userId.toString,
    "发送时间" -> new Timestamp(event.time * 100).toString,
  )
}
