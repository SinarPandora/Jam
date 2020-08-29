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
}

object ChatInfo {
  def apply(chatType: String, chatId: Long): ChatInfo = new ChatInfo(chatType, chatId)

  def apply(eventMessage: EventMessage): ChatInfo =
    eventMessage match {
      case message: EventGroupOrDiscussMessage =>
        new ChatInfo(message.getMessageType, message.getGroup.getId)
      case message: EventPrivateMessage =>
        new ChatInfo(MessageType.PRIVATE, message.getSenderId)
    }

  object None extends ChatInfo("None", -1L)
}
