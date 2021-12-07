package o.lartifa.jam.cool.qq.listener.event

import cc.moecraft.icq.event.events.notice.EventNoticeFriendPoke
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo

import java.sql.Timestamp

/**
 * 私聊拍一拍
 *
 * Example: 1211402231 戳 2062406606
 * {
 * "notice_type": "notify", [Support]
 * "post_type": "notice",   [Support]
 * "self_id": 2062406606,   [Support]
 * "sender_id": 1211402231, [Support]
 * "sub_type": "poke",      [Support]
 * "target_id": 2062406606, [Support]
 * "time": 1625586248,      [Partially]
 * "user_id": 1211402231    [Support]
 * }
 *
 * Author: sinar
 * 2021/7/6 22:29
 */
case class PokeEvent(event: EventNoticeFriendPoke) extends CQEvent("私聊拍一拍", event.getUserId) {
  /**
   * 会话信息
   */
  override val chatInfo: ChatInfo = ChatInfo(MessageType.PRIVATE, event.getUserId)

  /**
   * 该事件是否将不会被响应
   */
  override val willNotResponse: Boolean = event.getUserId == BotConfig.qID

  /**
   * 事件中的数据
   *
   * @return 数据
   */
  override val data: Map[String, String] = Map(
    // 自己信息
    "自己QQ" -> event.getSelfId.toString,
    "自己昵称" -> event.getBotAccount.getName,
    // 对方信息
    "发送者QQ" -> event.getUserId.toString,
    "发送者昵称" -> event.getUser.getInfo.getNickname,
    // Metadata
    "事件类型" -> event.getPostType,
    "通知类型" -> event.getNoticeType,
    "事件子类型" -> event.getSubType,
    "会话ID" -> event.getUserId.toString,
    "发送时间" -> new Timestamp(event.getTime * 100).toString
  )
}
