package o.lartifa.jam.onebot.protocol.event

import zio.blocks.schema.Schema

/**
 * OneBot v11 请求事件 wire。
 *
 * Author: sinar
 * 2026/11/28
 */
final case class WireRequestEvent(
  post_type: String,
  request_type: String,
  flag: String,
  user_id: Long,
  self_id: Long,
  time: Long,
  comment: Option[String] = None,
  sub_type: Option[String] = None,
  group_id: Option[Long] = None,
) derives Schema
