package jam.plugins.meme_maker.engine

import upickle.default._

/**
 * Meme API 响应体
 *
 * Author: sinar
 * 2020/11/18 23:32
 */
object MemeAPIResponse {
  case class Body(url: String)
  case class Response(ok: Boolean, body: Body, msg: String)

  implicit val responseRW: ReadWriter[Response] = macroRW[Response]
  implicit val bodyRW: ReadWriter[Body] = macroRW[Body]
}
