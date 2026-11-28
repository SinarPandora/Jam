package o.lartifa.jam.onebot.protocol.event

import o.lartifa.jam.onebot.domain.*
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import zio.blocks.schema.*

/**
 * 入站请求 JSON → 热路径 RequestEvent（好友申请 / 群邀请）。
 * `group`+`add` 等仍返回 None，交 legacy。
 *
 * Author: sinar
 * 2026/11/28
 */
object RequestEventCodec {

  def tryDecode(rawJson: String, outbound: OutboundGateway): Option[RequestEvent] =
    rawJson.fromJson[WireRequestEvent] match {
      case Left(_) => None
      case Right(wire) =>
        if (wire.post_type != "request") None
        else toDomain(wire, outbound)
    }

  def toDomain(wire: WireRequestEvent, outbound: OutboundGateway): Option[RequestEvent] = {
    val comment = wire.comment.getOrElse("")
    wire.request_type match {
      case "friend" =>
        Some(
          FriendAddRequest(
            postType = wire.post_type,
            requestType = wire.request_type,
            flag = wire.flag,
            userId = wire.user_id,
            selfId = wire.self_id,
            time = wire.time,
            comment = comment,
            outbound = outbound,
          ),
        )
      case "group" if wire.sub_type.contains("invite") =>
        for {
          gid <- wire.group_id
          st <- wire.sub_type
        } yield GroupInviteRequest(
          postType = wire.post_type,
          requestType = wire.request_type,
          flag = wire.flag,
          userId = wire.user_id,
          selfId = wire.self_id,
          time = wire.time,
          comment = comment,
          groupId = gid,
          subType = st,
          outbound = outbound,
        )
      case _ => None
    }
  }
}
