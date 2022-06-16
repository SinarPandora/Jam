package o.lartifa.jam.cool.qq.listener.event

import cc.moecraft.icq.event.events.notice.groupmember.decrease.EventNoticeGroupMemberDecrease
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.patterns.ContentMatcher.Events

import java.sql.Timestamp

/**
 * 群员减少事件
 * {
 * "group_id":935752317,
 * "notice_type":"group_decrease",
 * "operator_id":1211402231,
 * "post_type":"notice",
 * "self_id":2062406606,
 * "sub_type":"kick",
 * "time":1655388608,
 * "user_id":3219017931
 * }
 * Author: sinar
 * 2021/7/9 22:27
 */
case class MemberDecEvent(event: EventNoticeGroupMemberDecrease) extends CQEvent(Events.MemberDec.name, event.getOperatorId) {
  /**
   * 会话信息
   */
  override val chatInfo: ChatInfo = ChatInfo(MessageType.GROUP, event.getGroupId)

  /**
   * 该事件是否将不会被响应
   */
  override val willNotResponse: Boolean = event.getUserId == BotConfig.qID

  /**
   * 事件中的数据
   *
   * @return 数据
   */
  override val data: Map[String, String] = {
    val api = event.getHttpApi
    Map(
      // 群信息
      "群ID" -> event.getGroupId.toString,
      "群QQ" -> event.getGroupId.toString,
      "群名" -> event.getGroupMethods.getGroup.getInfo.getGroupName,
      // 被踢成员信息
      "群员QQ" -> event.getUserId.toString,
      "群员昵称" -> api.getStrangerInfo(event.getUserId).getData.getNickname,
      // 操作者信息
      "操作者QQ" -> event.getOperatorId.toString,
      "操作者昵称" -> (if (event.getOperatorId == 0) "" else api.getStrangerInfo(event.getOperatorId).getData.getNickname),
      "操作者群昵称" -> (if (event.getOperatorId == 0) "" else api.getGroupMemberInfo(event.getGroupId, event.getOperatorId).getData.getNickname),
      // Metadata
      "事件类型" -> event.getPostType,
      "通知类型" -> event.getNoticeType,
      "事件子类型" -> event.getSubType,
      "会话ID" -> event.getGroupId.toString,
      "发送时间" -> new Timestamp(event.getTime * 100).toString
    )
  }
}
