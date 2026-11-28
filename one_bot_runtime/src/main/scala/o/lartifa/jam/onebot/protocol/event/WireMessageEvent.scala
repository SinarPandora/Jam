package o.lartifa.jam.onebot.protocol.event

import zio.blocks.schema.Schema

/**
 * OneBot v11 消息事件 wire（字段名对齐协议 snake_case）。
 * 多余字段（如 sender 以外的扩展）由 Schema JSON 解码忽略。
 *
 * Author: sinar
 * 2026/11/27
 */
final case class WireSender(
  user_id: Option[Long] = None,
  nickname: Option[String] = None,
  card: Option[String] = None,
  role: Option[String] = None,
  age: Option[Int] = None,
  sex: Option[String] = None,
) derives Schema

final case class WireMessageEvent(
  post_type: String,
  message_type: String,
  message_id: Long,
  user_id: Long,
  self_id: Long,
  time: Long,
  message: String,
  raw_message: Option[String] = None,
  font: Option[Long] = None,
  sub_type: Option[String] = None,
  group_id: Option[Long] = None,
  discuss_id: Option[Long] = None,
  sender: Option[WireSender] = None,
) derives Schema
