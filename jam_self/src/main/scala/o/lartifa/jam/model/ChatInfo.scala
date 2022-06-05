package o.lartifa.jam.model

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import cc.moecraft.icq.sender.returndata.ReturnData
import cc.moecraft.icq.sender.returndata.returnpojo.send.RMessageReturnData
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.pool.JamContext

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

  implicit class ChatInfoReply(chatInfo: ChatInfo) {
    /**
     * 发送消息
     *
     * @param obj 消息对象
     * @return 发送结果
     */
    def sendMsg(obj: Any): ReturnData[RMessageReturnData] = {
      val client = JamContext.apiClient
      chatInfo.chatType match {
        case MessageType.PRIVATE => client.sendPrivateMsg(chatInfo.chatId, obj.toString)
        case MessageType.GROUP => client.sendGroupMsg(chatInfo.chatId, obj.toString)
        case MessageType.DISCUSS => client.sendDiscussMsg(chatInfo.chatId, obj.toString)
      }
    }
  }
}
