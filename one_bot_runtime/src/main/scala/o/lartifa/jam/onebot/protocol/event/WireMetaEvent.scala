package o.lartifa.jam.onebot.protocol.event

import zio.blocks.schema.Schema

/**
 * OneBot v11 元事件 wire（snake_case；status 等复杂字段忽略）。
 *
 * Author: sinar
 * 2026/11/28
 */
final case class WireMetaEvent(
  post_type: String,
  meta_event_type: String,
  self_id: Long,
  time: Long,
  interval: Option[Long] = None,
  sub_type: Option[String] = None,
) derives Schema
