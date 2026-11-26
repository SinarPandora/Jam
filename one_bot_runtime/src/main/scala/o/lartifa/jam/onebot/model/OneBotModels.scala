package o.lartifa.jam.onebot.model

import zio.blocks.schema.Schema

/**
 * OneBot v11 线协议模型（Schema 为真相来源）。
 *
 * Author: sinar
 * 2026/11/26
 */
final case class SendPrivateMsgParams(user_id: Long, message: String) derives Schema

final case class SendGroupMsgParams(group_id: Long, message: String) derives Schema

final case class SetGroupBanParams(group_id: Long, user_id: Long, duration: Long) derives Schema

/** 通用 OneBot HTTP 回执外壳（data 以原始 JSON 字符串保留，便于透传）。 */
final case class OneBotApiResponse(status: String, retcode: Int, data: Option[String] = None) derives Schema

final case class SendMsgResult(message_id: Long) derives Schema
