package o.lartifa.jam.cool.qq.listener.event

import cc.moecraft.icq.event.events.notice.EventNoticeFriendPoke
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo

import java.sql.Timestamp

/**
 * 戳一戳事件
 *
 * Example: 1211402231 戳 2062406606
 * {
 *  "notice_type": "notify", [Support]
 *  "post_type": "notice",   [Support]
 *  "self_id": 2062406606,   [Support]
 *  "sender_id": 1211402231, [Support]
 *  "sub_type": "poke",      [Support]
 *  "target_id": 2062406606, [Support]
 *  "time": 1625586248,      [Partially]
 *  "user_id": 1211402231    [Support]
 * }
 *
 * Author: sinar
 * 2021/7/6 22:29
 */
case class PokeEvent(event: EventNoticeFriendPoke) extends CQEvent {

  /**
   * 会话信息
   */
  override val chatInfo: ChatInfo = ChatInfo(MessageType.PRIVATE, event.getUserId)

  /**
   * 事件中的数据
   *
   * @return 数据
   */
  override val data: Map[String, String] = Map(
    "目标QQ" -> event.getTargetId.toString,
    "自己QQ" -> event.getSelfId.toString,
    "发送者QQ" -> event.getUser.getId.toString,
    "对方QQ" -> event.getUser.getId.toString,
    "发送者昵称" -> event.getUser.getInfo.getNickname,
    "对方昵称" -> event.getUser.getInfo.getNickname,
    "事件类型" -> event.getPostType,
    "通知类型" -> event.getNoticeType,
    "事件子类型" -> event.getSubType,
    "会话ID" -> event.getUserId.toString,
    "发送时间" -> new Timestamp(event.getTime * 100).toString
  )
}
