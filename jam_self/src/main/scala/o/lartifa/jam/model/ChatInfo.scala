package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.common.util.GlobalConstant.MessageType

/**
 * 会话信息结构体
 *
 * Author: sinar
 * 2020/1/14 22:46
 */
case class ChatInfo(chatType: String, chatId: Long) {
  override def toString: String = s"聊天类型：$chatType，会话 ID：$chatId"
  def serialize: String = s"${chatType}_$chatId"
}

object ChatInfo {

  def apply(serializeStr: String): ChatInfo = {
    val Array(chatType, chatId) = serializeStr.split("_")
    new ChatInfo(chatType, chatId.toLong)
  }

  def apply(chatType: String, chatId: Long): ChatInfo = new ChatInfo(chatType, chatId)

  def apply(eventMessage: EventMessage): ChatInfo =
    eventMessage match {
      case message: EventGroupOrDiscussMessage =>
        new ChatInfo(message.getMessageType, message.getGroup.getId)
      case message: EventPrivateMessage =>
        new ChatInfo(MessageType.PRIVATE, message.getSenderId)
      case null => None
    }

  object None extends ChatInfo(MessageType.NONE, -1L)
  object Group extends ChatInfo(MessageType.GROUP, -1L)
  object Private extends ChatInfo(MessageType.PRIVATE, -1L)
}
