package jam.plugins.meme_maker.v1.engine

/**
 * Meme API 响应体
 *
 * Author: sinar
 * 2020/11/18 23:32
 */
object MemeAPIV1Response {

  case class TemplatePair(id: Int, name: String, code: String)

}
