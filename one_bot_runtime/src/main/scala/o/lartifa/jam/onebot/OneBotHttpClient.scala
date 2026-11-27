package o.lartifa.jam.onebot

import o.lartifa.jam.onebot.model.*
import zio.*
import zio.blocks.schema.*
import zio.http.*

/**
 * OneBot HTTP 出站客户端（ZIO HTTP Client）。
 *
 * Author: sinar
 * 2026/11/26
 */
final class OneBotHttpClient(
  host: String,
  port: Int,
  accessToken: String = "",
) {

  private val base =
    if (host == "0.0.0.0" || host.isBlank) s"http://127.0.0.1:$port"
    else if (host.startsWith("http://") || host.startsWith("https://")) s"$host:$port"
    else s"http://$host:$port"

  /**
   * POST 到 OneBot API 节点，返回响应正文。
   *
   * @param apiPath 如 send_private_msg
   * @param jsonBody JSON 请求体
   */
  def postRaw(apiPath: String, jsonBody: String): String =
    ZioRuntime.runWithClient {
      for {
        url <- ZIO.fromEither(URL.decode(makeUrl(apiPath)))
        response <- Client.batched(
          Request
            .post(url, Body.fromString(jsonBody))
            .addHeader(Header.ContentType(MediaType.application.json))
        )
        body <- response.body.asString
      } yield body
    }

  def sendPrivateMsg(userId: Long, message: String, autoEscape: Boolean = false): String =
    postRaw("send_private_msg", SendPrivateMsgParams(userId, message, autoEscape).toJsonString)

  def sendGroupMsg(groupId: Long, message: String, autoEscape: Boolean = false): String =
    postRaw("send_group_msg", SendGroupMsgParams(groupId, message, autoEscape).toJsonString)

  def sendDiscussMsg(discussId: Long, message: String, autoEscape: Boolean = false): String =
    postRaw("send_discuss_msg", SendDiscussMsgParams(discussId, message, autoEscape).toJsonString)

  def setGroupBan(groupId: Long, userId: Long, duration: Long): String =
    postRaw("set_group_ban", SetGroupBanParams(groupId, userId, duration).toJsonString)

  /** 透传任意参数 Map 为 JSON 对象（适配 IcqHttpApi.send）。 */
  def postParams(apiPath: String, params: Map[String, Any]): String =
    postRaw(apiPath, encodeParams(params))

  private def makeUrl(apiPath: String): String = {
    val path = apiPath.stripPrefix("/")
    val withToken =
      if (accessToken.isEmpty) s"$base/$path"
      else s"$base/$path?access_token=$accessToken"
    withToken
  }

  private def encodeParams(params: Map[String, Any]): String = {
    val entries = params.map { case (k, v) =>
      val encoded = v match {
        case null                 => "null"
        case n: Number            => n.toString
        case b: java.lang.Boolean => b.toString
        case b: Boolean           => b.toString
        case s: String            => "\"" + escapeJson(s) + "\""
        case other                => "\"" + escapeJson(other.toString) + "\""
      }
      s""""$k":$encoded"""
    }
    entries.mkString("{", ",", "}")
  }

  private def escapeJson(s: String): String =
    s.flatMap {
      case '"'  => "\\\""
      case '\\' => "\\\\"
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case c    => c.toString
    }
}
