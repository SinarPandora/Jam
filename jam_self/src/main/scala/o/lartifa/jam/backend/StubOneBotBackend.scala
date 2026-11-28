package o.lartifa.jam.backend

import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.BotConfig
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl}
import o.lartifa.jam.pool.JamContext
import org.json4s.*
import org.json4s.jackson.JsonMethods.{compact, parse, render}
import java.io.{File, FileOutputStream, OutputStreamWriter}
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong
import com.sun.net.httpserver.{HttpExchange, HttpServer}

/**
 * 假 OneBot HTTP API：不连接真实协议端，只响应出站调用（ZIO OneBotHttpClient / 过渡期 HttpApi）并落盘，便于本地/验证驱动。
 *
 * Author: sinar
 * 2026/11/26
 */
object StubOneBotBackend {
  implicit private val formats: Formats = DefaultFormats

  private val messageIdSeq = new AtomicLong(1)
  @volatile private var serverOpt: Option[HttpServer] = None
  @volatile private var logWriterOpt: Option[OutputStreamWriter] = None

  /**
   * 启动 stub，监听 coolQQ.port.post；请求以 NDJSON 写入 logFile。
   *
   * @param logFile 出站调用日志
   */
  def start(logFile: File): Unit = {
    stop()
    logFile.getParentFile.mkdirs()
    if (logFile.exists()) logFile.delete()
    logFile.createNewFile()
    val writer = new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8)
    logWriterOpt = Some(writer)

    val host = if (postUrl == "0.0.0.0" || postUrl.isBlank) "127.0.0.1" else postUrl
    val server = HttpServer.create(new InetSocketAddress(host, postPort), 0)
    server.createContext("/", exchange => handle(exchange, writer))
    server.setExecutor(null)
    server.start()
    serverOpt = Some(server)

    val logger = Option(JamContext.loggerFactory.get()).map(_.system)
    val msg = s"${AnsiColor.GREEN}Stub OneBot API 已启动：$host:$postPort ，日志 → ${logFile.getAbsolutePath}"
    logger.foreach(_.log(msg))
    if (logger.isEmpty) println(msg)

    sys.addShutdownHook(stop())
  }

  /** 停止 stub（幂等） */
  def stop(): Unit = {
    serverOpt.foreach(_.stop(0))
    serverOpt = None
    logWriterOpt.foreach { w =>
      w.flush()
      w.close()
    }
    logWriterOpt = None
  }

  private def handle(exchange: HttpExchange, writer: OutputStreamWriter): Unit = {
    val path = Option(exchange.getRequestURI.getPath).getOrElse("/").stripPrefix("/").split('/').headOption.getOrElse("")
    val body = new String(exchange.getRequestBody.readAllBytes(), StandardCharsets.UTF_8)
    appendLog(writer, path, body)

    val response = compact(render(buildResponse(path)))
    val bytes = response.getBytes(StandardCharsets.UTF_8)
    exchange.getResponseHeaders.add("Content-Type", "application/json; charset=utf-8")
    exchange.sendResponseHeaders(200, bytes.length)
    val out = exchange.getResponseBody
    out.write(bytes)
    out.close()
  }

  private def appendLog(writer: OutputStreamWriter, path: String, body: String): Unit = synchronized {
    val bodyJson: JValue =
      if (body.isBlank) JNull
      else
        try parse(body)
        catch {
          case _: Exception => JString(body)
        }
    val payload = JObject(
      "ts" -> JLong(System.currentTimeMillis()),
      "path" -> JString(path),
      "body" -> bodyJson
    )
    writer.write(compact(render(payload)))
    writer.write('\n')
    writer.flush()
  }

  private def buildResponse(path: String): JObject = {
    val api = path.stripSuffix("_async").stripSuffix("_rate_limited")
    val data: JValue = api match {
      case "get_login_info" =>
        JObject(
          "user_id" -> JLong(BotConfig.qID),
          "nickname" -> JString("JamStub")
        )
      case "send_private_msg" | "send_group_msg" | "send_discuss_msg" =>
        JObject("message_id" -> JLong(messageIdSeq.getAndIncrement()))
      case "get_stranger_info" | "get_group_member_info" =>
        JObject(
          "user_id" -> JLong(0),
          "nickname" -> JString("stub"),
          "card" -> JString(""),
          "sex" -> JString("unknown"),
          "age" -> JInt(0),
          "area" -> JString(""),
          "level" -> JString(""),
          "role" -> JString("member"),
          "title" -> JString(""),
          "unfriendly" -> JBool(false)
        )
      case "get_group_info" =>
        JObject(
          "group_id" -> JLong(713201079L),
          "group_name" -> JString("jam-verify")
        )
      case "get_group_list" =>
        // Picq AccountManager indexes bots by group membership; empty list → NPE on group events
        JArray(List(
          JObject("group_id" -> JLong(713201079L), "group_name" -> JString("jam-verify")),
          JObject("group_id" -> JLong(1L), "group_name" -> JString("stub-fallback"))
        ))
      case "get_group_member_list" | "get_friend_list" | "_get_friend_list" =>
        JArray(Nil)
      case "can_send_image" | "can_send_record" =>
        JObject("yes" -> JBool(true))
      case "get_status" =>
        JObject("online" -> JBool(true), "good" -> JBool(true))
      case "get_version_info" =>
        JObject(
          "app_name" -> JString("jam-stub-onebot"),
          "app_version" -> JString("0.1.0"),
          "protocol_version" -> JString("v11")
        )
      case _ =>
        JObject()
    }
    JObject(
      "status" -> JString("ok"),
      "retcode" -> JInt(0),
      "data" -> data
    )
  }
}
