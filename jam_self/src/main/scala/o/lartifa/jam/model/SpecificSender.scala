package o.lartifa.jam.model

import o.lartifa.jam.onebot.domain.ChatMessage
import o.lartifa.jam.common.util.GlobalConstant.MessageType

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
  def apply(eventMessage: ChatMessage): SpecificSender = {
    new SpecificSender(ChatInfo(eventMessage), eventMessage.senderId)
  }

  /**
   * 创建私聊消息发送者
   *
   * @param qid QQ 号
   * @return 指定发送者
   */
  def privateOf(qid: Long): SpecificSender = {
    new SpecificSender(ChatInfo(MessageType.PRIVATE, qid), qid)
  }

  /**
   * 创建群组消息发送者
   *
   * @param gid 群号
   * @param qid QQ 号
   * @return 指定发送者
   */
  def groupOf(gid: Long, qid: Long): SpecificSender = {
    new SpecificSender(ChatInfo(MessageType.GROUP, gid), qid)
  }
}
