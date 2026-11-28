package o.lartifa.jam.onebot.protocol.event

import o.lartifa.jam.onebot.domain.*
import zio.blocks.schema.*

/**
 * 入站元事件 JSON → MetaEvent。
 *
 * Author: sinar
 * 2026/11/28
 */
object MetaEventCodec {

  def tryDecode(rawJson: String): Option[MetaEvent] =
    rawJson.fromJson[WireMetaEvent] match {
      case Left(_) => None
      case Right(wire) =>
        if (wire.post_type != "meta_event") None
        else toDomain(wire)
    }

  def toDomain(wire: WireMetaEvent): Option[MetaEvent] =
    wire.meta_event_type match {
      case "heartbeat" =>
        Some(
          MetaHeartbeat(
            postType = wire.post_type,
            metaEventType = wire.meta_event_type,
            selfId = wire.self_id,
            time = wire.time,
            interval = wire.interval,
          ),
        )
      case "lifecycle" =>
        Some(
          MetaLifecycle(
            postType = wire.post_type,
            metaEventType = wire.meta_event_type,
            selfId = wire.self_id,
            time = wire.time,
            subType = wire.sub_type,
          ),
        )
      case _ => None
    }
}