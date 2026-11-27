package o.lartifa.jam.onebot.protocol.event

import o.lartifa.jam.onebot.cq.Cq
import o.lartifa.jam.onebot.domain.*
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import zio.blocks.schema.*

/**
 * 入站消息 JSON → 领域 ChatMessage。
 *
 * Author: sinar
 * 2026/11/27
 */
object MessageEventCodec {

  /**
   * 仅当 `post_type=message` 且能识别 message_type 时返回 Some。
   */
  def tryDecode(rawJson: String, outbound: OutboundGateway): Option[ChatMessage] =
    rawJson.fromJson[WireMessageEvent] match {
      case Left(_) => None
      case Right(wire) =>
        if (wire.post_type != "message") None
        else toDomain(wire, outbound)
    }

  def toDomain(wire: WireMessageEvent, outbound: OutboundGateway): Option[ChatMessage] = {
    val raw = wire.raw_message.getOrElse(wire.message)
    val decoded = Cq.decodeMessage(wire.message)
    val sender = toSender(wire)
    val font = wire.font.getOrElse(0L)
    wire.message_type match {
      case "private" =>
        Some(
          PrivateMessage(
            postType = wire.post_type,
            messageType = wire.message_type,
            messageId = wire.message_id,
            senderId = wire.user_id,
            selfId = wire.self_id,
            time = wire.time,
            message = decoded,
            rawMessage = raw,
            font = font,
            subType = wire.sub_type,
            sender = sender,
            outbound = outbound,
          ),
        )
      case "group" =>
        wire.group_id.map { gid =>
          GroupMessage(
            postType = wire.post_type,
            messageType = wire.message_type,
            messageId = wire.message_id,
            senderId = wire.user_id,
            selfId = wire.self_id,
            time = wire.time,
            message = decoded,
            rawMessage = raw,
            font = font,
            subType = wire.sub_type,
            groupId = gid,
            sender = sender,
            outbound = outbound,
          )
        }
      case "discuss" =>
        wire.discuss_id.map { did =>
          DiscussMessage(
            postType = wire.post_type,
            messageType = wire.message_type,
            messageId = wire.message_id,
            senderId = wire.user_id,
            selfId = wire.self_id,
            time = wire.time,
            message = decoded,
            rawMessage = raw,
            font = font,
            subType = wire.sub_type,
            discussId = did,
            sender = sender,
            outbound = outbound,
          )
        }
      case _ => None
    }
  }

  private def toSender(wire: WireMessageEvent): SenderInfo = {
    val snap = wire.sender
    SenderInfo(
      userId = snap.flatMap(_.user_id).getOrElse(wire.user_id),
      nickname = snap.flatMap(_.nickname),
      card = snap.flatMap(_.card),
      role = snap.flatMap(_.role),
      age = snap.flatMap(_.age),
      sex = snap.flatMap(_.sex),
    )
  }
}
