package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}

/**
 * 会话信息结构体
 *
 * Author: sinar
 * 2020/1/14 22:46 
 */
case class ChatInfo(chatType: String, chatId: Long)

object ChatInfo {
  def apply(chatType: String, chatId: Long): ChatInfo = new ChatInfo(chatType, chatId)

  def apply(eventMessage: EventMessage): ChatInfo = {
    eventMessage match {
      case message: EventGroupOrDiscussMessage =>
        new ChatInfo(eventMessage.getMessageType, message.getGroup.getId)
      case message: EventPrivateMessage =>
        new ChatInfo(eventMessage.getMessageType, message.getSenderId)
    }
  }
}
