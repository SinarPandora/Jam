package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.EventMessage

/**
 * 指定消息发送者
 *
 * Author: sinar
 * 2021/8/16 23:46
 */
case class SpecificSender(chat: ChatInfo, qid: Long) {
  override def toString: String = s"QQ号 $qid，${chat.toString}"
}

object SpecificSender {
  def apply(eventMessage: EventMessage): SpecificSender = {
    new SpecificSender(ChatInfo(eventMessage), eventMessage.getSenderId)
  }
}
