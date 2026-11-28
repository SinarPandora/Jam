package o.lartifa.jam.onebot.model

import zio.blocks.schema.Schema

/**
 * OneBot v11 线协议模型（Schema 为真相来源）。
 *
 * Author: sinar
 * 2026/11/26
 */
final case class SendPrivateMsgParams(
  user_id: Long,
  message: String,
  auto_escape: Boolean = false,
) derives Schema

final case class SendGroupMsgParams(
  group_id: Long,
  message: String,
  auto_escape: Boolean = false,
) derives Schema

final case class SendDiscussMsgParams(
  discuss_id: Long,
  message: String,
  auto_escape: Boolean = false,
) derives Schema

final case class SetGroupBanParams(group_id: Long, user_id: Long, duration: Long) derives Schema

final case class SetFriendAddRequestParams(
  flag: String,
  approve: Boolean,
  remark: String = "",
) derives Schema

final case class SetGroupAddRequestParams(
  flag: String,
  `type`: String,
  approve: Boolean,
  reason: String = "",
) derives Schema

final case class SendMsgResult(message_id: Long) derives Schema

/** 通用 OneBot HTTP 回执（data 为嵌套对象）。 */
final case class OneBotApiResponse(
  status: String,
  retcode: Int,
  data: Option[SendMsgResult] = None,
) derives Schema
