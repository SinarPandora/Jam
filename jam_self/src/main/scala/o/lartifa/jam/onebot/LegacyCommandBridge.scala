package o.lartifa.jam.onebot

import o.lartifa.jam.ob.PicqBotX
import o.lartifa.jam.ob.event.events.message.{EventDiscussMessage, EventGroupMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.onebot.domain.{ChatMessage, DiscussMessage, GroupMessage, PrivateMessage, SenderInfo}
import o.lartifa.jam.pool.JamContext

/**
 * 临时桥：把领域 ChatMessage 填进 legacy EventMessage，仅供 CommandListener.check。
 * 后续「指令」刀删除。
 *
 * Author: sinar
 * 2026/11/27
 */
object LegacyCommandBridge {

  /**
   * @return true 表示继续交给 QMessageListener；false 表示指令已吞掉
   */
  def tryHandle(chat: ChatMessage, bot: PicqBotX): Boolean = {
    val listener = bot.getEventManager.getCommandListener
    if (listener == null) true
    else listener.check(toLegacyEvent(chat, bot))
  }

  def toLegacyEvent(chat: ChatMessage, bot: PicqBotX): EventMessage = {
    val event: EventMessage = chat match {
      case g: GroupMessage =>
        val e = new EventGroupMessage()
        e.setGroupId(g.groupId)
        e.setSubType(g.subType.orNull)
        e
      case d: DiscussMessage =>
        val e = new EventDiscussMessage()
        e.setDiscussId(d.discussId)
        e
      case _: PrivateMessage =>
        val e = new EventPrivateMessage()
        e.setSubType(chat.subType.orNull)
        e
    }
    event.setBot(bot)
    event.setPostType(chat.postType)
    event.setMessageType(chat.messageType)
    event.setMessageId(chat.messageId)
    event.setSenderId(chat.senderId)
    event.setSelfId(chat.selfId)
    event.setTime(chat.time)
    event.setMessage(chat.message)
    event.setRawMessage(chat.rawMessage)
    event.setFont(chat.font)
    event
  }

  /**
   * legacy EventMessage → 领域 ChatMessage（给仍收 EventMessage 的管理指令调用迁移后的 API 用）。
   */
  def fromLegacyEvent(event: EventMessage): ChatMessage = {
    val outbound = JamContext.outbound.get()
    val senderId = event.getSenderId.longValue
    val common = (
      event.getPostType,
      event.getMessageType,
      Option(event.getMessageId).fold(-1L)(_.longValue),
      senderId,
      Option(event.getSelfId).fold(-1L)(_.longValue),
      Option(event.getTime).fold(0L)(_.longValue),
      event.getMessage,
      event.getRawMessage,
      Option(event.getFont).fold(0L)(_.longValue),
    )
    val (postType, messageType, messageId, sid, selfId, time, message, rawMessage, font) = common
    event match {
      case g: EventGroupMessage =>
        GroupMessage(postType, messageType, messageId, sid, selfId, time, message, rawMessage, font,
          Option(g.getSubType), g.getGroupId.longValue, SenderInfo(sid), outbound)
      case d: EventDiscussMessage =>
        DiscussMessage(postType, messageType, messageId, sid, selfId, time, message, rawMessage, font,
          None, d.getDiscussId.longValue, SenderInfo(sid), outbound)
      case p: EventPrivateMessage =>
        PrivateMessage(postType, messageType, messageId, sid, selfId, time, message, rawMessage, font,
          Option(p.getSubType), SenderInfo(sid), outbound)
    }
  }
}
