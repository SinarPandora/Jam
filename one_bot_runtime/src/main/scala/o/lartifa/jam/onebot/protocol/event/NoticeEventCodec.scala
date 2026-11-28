package o.lartifa.jam.onebot.protocol.event

import o.lartifa.jam.onebot.domain.*
import zio.blocks.schema.*

/**
 * 入站通知 JSON → 热路径 NoticeEvent；非热路径返回 None（交 legacy Parser）。
 *
 * Author: sinar
 * 2026/11/28
 */
object NoticeEventCodec {

  def tryDecode(rawJson: String): Option[NoticeEvent] =
    rawJson.fromJson[WireNoticeEvent] match {
      case Left(_) => None
      case Right(wire) =>
        if (wire.post_type != "notice") None
        else toDomain(wire)
    }

  def toDomain(wire: WireNoticeEvent): Option[NoticeEvent] =
    wire.notice_type match {
      case "notify" if wire.sub_type.contains("poke") =>
        wire.group_id match {
          case Some(gid) =>
            wire.target_id.map { tid =>
              GroupPokeNotice(
                postType = wire.post_type,
                noticeType = wire.notice_type,
                selfId = wire.self_id,
                userId = wire.user_id,
                time = wire.time,
                subType = wire.sub_type,
                groupId = gid,
                targetId = tid,
              )
            }
          case None =>
            wire.target_id.map { tid =>
              FriendPokeNotice(
                postType = wire.post_type,
                noticeType = wire.notice_type,
                selfId = wire.self_id,
                userId = wire.user_id,
                time = wire.time,
                subType = wire.sub_type,
                targetId = tid,
              )
            }
        }
      case "group_increase" =>
        wire.group_id.map { gid =>
          GroupMemberIncreaseNotice(
            postType = wire.post_type,
            noticeType = wire.notice_type,
            selfId = wire.self_id,
            userId = wire.user_id,
            time = wire.time,
            subType = wire.sub_type,
            groupId = gid,
            operatorId = wire.operator_id.getOrElse(0L),
          )
        }
      case "group_decrease" =>
        // kick_me 等也走领域；业务侧 willNotResponse 过滤自身
        wire.group_id.map { gid =>
          GroupMemberDecreaseNotice(
            postType = wire.post_type,
            noticeType = wire.notice_type,
            selfId = wire.self_id,
            userId = wire.user_id,
            time = wire.time,
            subType = wire.sub_type,
            groupId = gid,
            operatorId = wire.operator_id.getOrElse(0L),
          )
        }
      case _ => None
    }
}
