package o.lartifa.jam.onebot.protocol.event

import zio.blocks.schema.Schema

/**
 * OneBot v11 通知事件 wire（snake_case；多余字段忽略）。
 *
 * Author: sinar
 * 2026/11/28
 */
final case class WireNoticeEvent(
  post_type: String,
  notice_type: String,
  self_id: Long,
  user_id: Long,
  time: Long,
  sub_type: Option[String] = None,
  group_id: Option[Long] = None,
  target_id: Option[Long] = None,
  operator_id: Option[Long] = None,
  sender_id: Option[Long] = None,
) derives Schema
