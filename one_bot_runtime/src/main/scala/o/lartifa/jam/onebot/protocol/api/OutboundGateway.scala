package o.lartifa.jam.onebot.protocol.api

import o.lartifa.jam.onebot.OneBotHttpClient
import o.lartifa.jam.onebot.model.*
import zio.blocks.schema.*

/**
 * 同步出站门面：干净 API，不暴露 legacy HttpApi。
 *
 * Author: sinar
 * 2026/11/27
 */
trait OutboundGateway {
  def sendPrivate(userId: Long, message: String, autoEscape: Boolean = false): Long

  def sendGroup(groupId: Long, message: String, autoEscape: Boolean = false): Long

  def sendDiscuss(discussId: Long, message: String, autoEscape: Boolean = false): Long

  def setFriendAddRequest(flag: String, approve: Boolean, remark: String = ""): Unit

  def setGroupAddRequest(
    flag: String,
    requestType: String,
    approve: Boolean,
    reason: String = "",
  ): Unit
}

object OutboundGateway {

  def apply(client: OneBotHttpClient): OutboundGateway =
    new HttpOutboundGateway(client)

  def apply(host: String, port: Int, accessToken: String = ""): OutboundGateway =
    apply(new OneBotHttpClient(host, port, accessToken))

  private final class HttpOutboundGateway(client: OneBotHttpClient) extends OutboundGateway {
    def sendPrivate(userId: Long, message: String, autoEscape: Boolean = false): Long =
      if (message == null || message.isEmpty) -1L
      else
        parseMessageId(
          client.postRaw(
            "send_private_msg",
            SendPrivateMsgParams(userId, message, autoEscape).toJsonString,
          ),
        )

    def sendGroup(groupId: Long, message: String, autoEscape: Boolean = false): Long =
      if (message == null || message.isEmpty) -1L
      else
        parseMessageId(
          client.postRaw(
            "send_group_msg",
            SendGroupMsgParams(groupId, message, autoEscape).toJsonString,
          ),
        )

    def sendDiscuss(discussId: Long, message: String, autoEscape: Boolean = false): Long =
      if (message == null || message.isEmpty) -1L
      else
        parseMessageId(
          client.postRaw(
            "send_discuss_msg",
            SendDiscussMsgParams(discussId, message, autoEscape).toJsonString,
          ),
        )

    def setFriendAddRequest(flag: String, approve: Boolean, remark: String = ""): Unit =
      client.postRaw(
        "set_friend_add_request",
        SetFriendAddRequestParams(flag, approve, remark).toJsonString,
      ): Unit

    def setGroupAddRequest(
      flag: String,
      requestType: String,
      approve: Boolean,
      reason: String = "",
    ): Unit =
      client.postRaw(
        "set_group_add_request",
        SetGroupAddRequestParams(flag, requestType, approve, reason).toJsonString,
      ): Unit

    private def parseMessageId(body: String): Long =
      body.fromJson[OneBotApiResponse] match {
        case Right(resp) => resp.data.map(_.message_id).getOrElse(-1L)
        case Left(_)     => -1L
      }
  }
}
