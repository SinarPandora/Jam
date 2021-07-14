package o.lartifa.jam.cool.qq.listener.event
import cc.moecraft.icq.event.events.notice.EventNoticeGroupPoke
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo

import java.sql.Timestamp

/**
 * 群聊拍一拍事件
 *
 * Example: 在群 935752317 中，1211402231 戳 1580265059
 * {
 *   "group_id": 935752317,    [Support]
 *   "notice_type": "notify",  [Support]
 *   "post_type": "notice",    [Support]
 *   "self_id": 2062406606,    [Useless]
 *   "sender_id": 1211402231,  [Support]
 *   "sub_type": "poke",       [Support]
 *   "target_id": 1580265059,  [Support]
 *   "time": 1625890531,       [Partially]
 *   "user_id": 1211402231     [Support]
 * }
 *
 * Author: sinar
 * 2021/7/9 22:25
 */
case class PokeInGroupEvent(event: EventNoticeGroupPoke) extends CQEvent("群聊拍一拍") {
  /**
   * 会话信息
   */
  override val chatInfo: ChatInfo = ChatInfo(MessageType.GROUP, event.getGroupId)
  /**
   * 事件中的数据
   *
   * @return 数据
   */
  override val data: Map[String, String] = Map(
    // 目标信息
    "目标QQ" -> event.getTargetId.toString,
    "目标群昵称" -> event.getTargetGroupUser.getInfo.getNickname,
    // 发送者信息
    "发送者QQ" -> event.getSenderGroupUser.getId.toString,
    "发送者群昵称" -> event.getSenderGroupUser.getInfo.getNickname,
    // 群信息
    "群号" -> event.getGroup.getId.toString,
    "群名" -> event.getGroup.getInfo.getGroupName,
    // Metadata
    "事件类型" -> event.getPostType,
    "通知类型" -> event.getNoticeType,
    "事件子类型" -> event.getSubType,
    "会话ID" -> event.getUserId.toString,
    "发送时间" -> new Timestamp(event.getTime * 100).toString
  )
}
