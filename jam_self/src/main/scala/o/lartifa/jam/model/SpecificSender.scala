package o.lartifa.jam.model

/**
 * 指定消息发送者
 *
 * Author: sinar
 * 2021/8/16 23:46
 */
case class SpecificSender(chat: ChatInfo, qid: Long) {
  override def toString: String = s"QQ号 $qid，${chat.toString}"
}
