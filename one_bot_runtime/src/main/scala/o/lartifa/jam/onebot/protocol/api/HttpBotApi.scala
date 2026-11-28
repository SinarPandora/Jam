package o.lartifa.jam.onebot.protocol.api

import o.lartifa.jam.onebot.OneBotHttpClient
import o.lartifa.jam.onebot.model.*
import zio.blocks.schema.toJsonString

import scala.util.Try

/**
 * 基于 OneBotHttpClient 的 BotApi 实现；响应用轻量 JSON 解析（避免 Gson ReturnData）。
 *
 * Author: sinar
 * 2026/11/28
 */
final class HttpBotApi(client: OneBotHttpClient) extends BotApi {
  private val outbound = OutboundGateway(client)

  def sendPrivateMsg(userId: Long, message: String, autoEscape: Boolean = false): Long =
    outbound.sendPrivate(userId, message, autoEscape)

  def sendGroupMsg(groupId: Long, message: String, autoEscape: Boolean = false): Long =
    outbound.sendGroup(groupId, message, autoEscape)

  def sendDiscussMsg(discussId: Long, message: String, autoEscape: Boolean = false): Long =
    outbound.sendDiscuss(discussId, message, autoEscape)

  def setGroupBan(groupId: Long, userId: Long, durationSeconds: Long): Unit =
    client.postRaw("set_group_ban", SetGroupBanParams(groupId, userId, durationSeconds).toJsonString): Unit

  def setGroupWholeBan(groupId: Long, enable: Boolean): Unit =
    client.postParams("set_group_whole_ban", Map("group_id" -> groupId, "enable" -> enable)): Unit

  def setGroupKick(groupId: Long, userId: Long, rejectFurtherRequest: Boolean = false): Unit =
    client.postParams(
      "set_group_kick",
      Map("group_id" -> groupId, "user_id" -> userId, "reject_add_request" -> rejectFurtherRequest),
    ): Unit

  def setGroupCard(groupId: Long, userId: Long, card: String): Unit =
    client.postParams("set_group_card", Map("group_id" -> groupId, "user_id" -> userId, "card" -> card)): Unit

  def setGroupAdmin(groupId: Long, userId: Long, enable: Boolean): Unit =
    client.postParams("set_group_admin", Map("group_id" -> groupId, "user_id" -> userId, "enable" -> enable)): Unit

  def setGroupLeave(groupId: Long, dismiss: Boolean = false): Unit =
    client.postParams("set_group_leave", Map("group_id" -> groupId, "is_dismiss" -> dismiss)): Unit

  def setGroupSpecialTitle(groupId: Long, userId: Long, specialTitle: String, duration: Long = -1L): Unit =
    client.postParams(
      "set_group_special_title",
      Map("group_id" -> groupId, "user_id" -> userId, "special_title" -> specialTitle, "duration" -> duration),
    ): Unit

  def setDiscussLeave(discussId: Long): Unit =
    client.postParams("set_discuss_leave", Map("discuss_id" -> discussId)): Unit

  def setFriendAddRequest(flag: String, approve: Boolean, remark: String = ""): Unit =
    outbound.setFriendAddRequest(flag, approve, remark)

  def setGroupAddRequest(flag: String, requestType: String, approve: Boolean, reason: String = ""): Unit =
    outbound.setGroupAddRequest(flag, requestType, approve, reason)

  def getLoginInfo(): Option[LoginInfo] =
    parseObj(client.postParams("get_login_info", Map.empty)).map { m =>
      LoginInfo(longOf(m, "user_id"), strOf(m, "nickname"))
    }

  def getStrangerInfo(userId: Long, noCache: Boolean = false): Option[StrangerInfo] =
    parseObj(client.postParams("get_stranger_info", Map("user_id" -> userId, "no_cache" -> noCache))).map { m =>
      StrangerInfo(
        userId = longOf(m, "user_id", userId),
        nickname = strOf(m, "nickname"),
        sex = strOf(m, "sex", "unknown"),
        age = intOf(m, "age"),
      )
    }

  def getGroupInfo(groupId: Long, noCache: Boolean = false): Option[GroupInfo] =
    parseObj(client.postParams("get_group_info", Map("group_id" -> groupId, "no_cache" -> noCache))).map { m =>
      GroupInfo(longOf(m, "group_id", groupId), strOf(m, "group_name"))
    }

  def getGroupMemberInfo(groupId: Long, userId: Long, noCache: Boolean = false): Option[GroupMemberInfo] =
    parseObj(
      client.postParams(
        "get_group_member_info",
        Map("group_id" -> groupId, "user_id" -> userId, "no_cache" -> noCache),
      ),
    ).map(m => memberFrom(m, groupId))

  def getGroupMemberList(groupId: Long): List[GroupMemberInfo] =
    parseArr(client.postParams("get_group_member_list", Map("group_id" -> groupId)))
      .map(m => memberFrom(m, groupId))

  def getFriendList(): List[FriendInfo] =
    parseArr(client.postParams("get_friend_list", Map.empty)).map { m =>
      FriendInfo(longOf(m, "user_id"), strOf(m, "nickname"), strOf(m, "remark"))
    }

  def getGroupList(): List[GroupSummary] =
    parseArr(client.postParams("get_group_list", Map.empty)).map { m =>
      GroupSummary(longOf(m, "group_id"), strOf(m, "group_name"))
    }

  private def memberFrom(m: Map[String, Any], defaultGroup: Long): GroupMemberInfo =
    GroupMemberInfo(
      userId = longOf(m, "user_id"),
      nickname = strOf(m, "nickname"),
      card = strOf(m, "card"),
      sex = strOf(m, "sex", "unknown"),
      age = intOf(m, "age"),
      role = strOf(m, "role", "member"),
      unfriendly = boolOf(m, "unfriendly"),
      groupId = longOf(m, "group_id", defaultGroup),
    )

  /** 极简 JSON 对象/数组解析（只覆盖 OneBot 回包需要的标量与一层对象/数组）。 */
  private def parseObj(body: String): Option[Map[String, Any]] =
    extractData(body).collect { case m: Map[?, ?] @unchecked => m.asInstanceOf[Map[String, Any]] }

  private def parseArr(body: String): List[Map[String, Any]] =
    extractData(body) match {
      case Some(list: List[?]) =>
        list.collect { case m: Map[?, ?] @unchecked => m.asInstanceOf[Map[String, Any]] }
      case _ => Nil
    }

  private def extractData(body: String): Option[Any] =
    Try(JsonLite.parse(body)).toOption.flatMap {
      case root: Map[?, ?] @unchecked =>
        val m = root.asInstanceOf[Map[String, Any]]
        m.get("data")
      case other => Some(other)
    }

  private def longOf(m: Map[String, Any], key: String, default: Long = 0L): Long =
    m.get(key).map {
      case n: Number => n.longValue
      case s: String => Try(s.toLong).getOrElse(default)
      case _         => default
    }.getOrElse(default)

  private def intOf(m: Map[String, Any], key: String, default: Int = 0): Int =
    m.get(key).map {
      case n: Number => n.intValue
      case s: String => Try(s.toInt).getOrElse(default)
      case _         => default
    }.getOrElse(default)

  private def strOf(m: Map[String, Any], key: String, default: String = ""): String =
    m.get(key).map {
      case null => default
      case s    => s.toString
    }.getOrElse(default)

  private def boolOf(m: Map[String, Any], key: String, default: Boolean = false): Boolean =
    m.get(key).map {
      case b: Boolean           => b
      case b: java.lang.Boolean => b.booleanValue
      case s: String            => s.equalsIgnoreCase("true") || s == "1"
      case n: Number            => n.intValue != 0
      case _                   => default
    }.getOrElse(default)
}

/** 无依赖的轻量 JSON → Map/List/标量（仅够 OneBot data 解析）。 */
private object JsonLite {
  def parse(input: String): Any = {
    val s = input.trim
    if (s.isEmpty) Map.empty[String, Any]
    else new Parser(s).parseValue()
  }

  private final class Parser(s: String) {
    private var i = 0

    def parseValue(): Any = {
      skipWs()
      if (i >= s.length) return null
      s.charAt(i) match {
        case '{' => parseObject()
        case '[' => parseArray()
        case '"' => parseString()
        case 't' => parseLiteral("true", true)
        case 'f' => parseLiteral("false", false)
        case 'n' => parseLiteral("null", null)
        case c if c == '-' || c.isDigit => parseNumber()
        case c => throw new IllegalArgumentException(s"Unexpected '$c' at $i")
      }
    }

    private def parseObject(): Map[String, Any] = {
      i += 1
      val b = Map.newBuilder[String, Any]
      skipWs()
      if (peek('}')) { i += 1; return b.result() }
      while (true) {
        skipWs()
        val key = parseString()
        skipWs()
        expect(':')
        val value = parseValue()
        b += key -> value
        skipWs()
        if (peek('}')) { i += 1; return b.result() }
        expect(',')
      }
      b.result()
    }

    private def parseArray(): List[Any] = {
      i += 1
      val b = List.newBuilder[Any]
      skipWs()
      if (peek(']')) { i += 1; return b.result() }
      while (true) {
        b += parseValue()
        skipWs()
        if (peek(']')) { i += 1; return b.result() }
        expect(',')
      }
      b.result()
    }

    private def parseString(): String = {
      expect('"')
      val sb = new StringBuilder
      while (i < s.length) {
        val c = s.charAt(i)
        i += 1
        c match {
          case '"' => return sb.result()
          case '\\' =>
            if (i >= s.length) throw new IllegalArgumentException("bad escape")
            val e = s.charAt(i)
            i += 1
            e match {
              case '"' | '\\' | '/' => sb.append(e)
              case 'b' => sb.append('\b')
              case 'f' => sb.append('\f')
              case 'n' => sb.append('\n')
              case 'r' => sb.append('\r')
              case 't' => sb.append('\t')
              case 'u' =>
                val hex = s.substring(i, i + 4)
                i += 4
                sb.append(Integer.parseInt(hex, 16).toChar)
              case other => sb.append(other)
            }
          case other => sb.append(other)
        }
      }
      throw new IllegalArgumentException("unterminated string")
    }

    private def parseNumber(): Any = {
      val start = i
      if (peek('-')) i += 1
      while (i < s.length && s.charAt(i).isDigit) i += 1
      var isDouble = false
      if (peek('.')) {
        isDouble = true
        i += 1
        while (i < s.length && s.charAt(i).isDigit) i += 1
      }
      if (peek('e') || peek('E')) {
        isDouble = true
        i += 1
        if (peek('+') || peek('-')) i += 1
        while (i < s.length && s.charAt(i).isDigit) i += 1
      }
      val raw = s.substring(start, i)
      if (isDouble) raw.toDouble
      else {
        val l = raw.toLong
        if (l >= Int.MinValue && l <= Int.MaxValue) l.toInt else l
      }
    }

    private def parseLiteral(lit: String, value: Any): Any = {
      if (!s.startsWith(lit, i)) throw new IllegalArgumentException(s"expected $lit")
      i += lit.length
      value
    }

    private def skipWs(): Unit =
      while (i < s.length && Character.isWhitespace(s.charAt(i))) i += 1

    private def peek(c: Char): Boolean = i < s.length && s.charAt(i) == c

    private def expect(c: Char): Unit = {
      skipWs()
      if (!peek(c)) throw new IllegalArgumentException(s"expected '$c' at $i")
      i += 1
    }
  }
}
