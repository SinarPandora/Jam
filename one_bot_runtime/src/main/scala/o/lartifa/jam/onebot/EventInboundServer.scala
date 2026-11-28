package o.lartifa.jam.onebot

import zio.*
import zio.http.*

/**
 * OneBot HTTP 反向入站：接收协议端 POST 的事件 JSON。
 *
 * Author: sinar
 * 2026/11/26
 */
object EventInboundServer {

  /**
   * 构建入站路由。成功处理返回 204（与 PicqHttpServer 对齐）。
   *
   * @param onEvent 原始 JSON 回调（由 jam_self 领域 codec 分流）
   */
  def routes(onEvent: String => Unit): Routes[Any, Response] =
    Routes(
      Method.POST / Root -> handler { (req: Request) =>
        val contentType = req.headers.get(Header.ContentType).map(_.renderedValue).getOrElse("")
        if (!contentType.toLowerCase.contains("application/json")) {
          ZIO.succeed(Response.text("Oh hi there! How are you?"))
        } else {
          req.body.asString
            .mapError(err => Response.internalServerError(err.getMessage))
            .flatMap { data =>
              ZIO
                .attempt(onEvent(data))
                .mapError(err => Response.internalServerError(err.getMessage))
                .as(Response.status(Status.NoContent))
            }
        }
      }
    )

  /**
   * 在给定端口启动入站服务（阻塞式 ZIO；调用方应 fork）。
   *
   * @param port    监听端口（coolQQ.port.socket）
   * @param onEvent 原始 JSON 回调
   */
  def serve(port: Int, onEvent: String => Unit): ZIO[Any, Throwable, Nothing] =
    Server.serve(routes(onEvent)).provide(Server.defaultWithPort(port))

  /**
   * 启动常驻入站服务（非阻塞，立即返回）。
   */
  def start(port: Int, onEvent: String => Unit): Fiber.Runtime[Throwable, Nothing] =
    ZioRuntime.forkDaemon(serve(port, onEvent))
}
