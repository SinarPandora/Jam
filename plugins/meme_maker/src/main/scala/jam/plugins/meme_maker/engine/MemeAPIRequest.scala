package jam.plugins.meme_maker.engine

import upickle.default._

/**
 * API 请求生成器
 *
 * Author: sinar
 * 2020/11/18 22:25
 */
case class MemeAPIRequest(id: Int, fillings: Map[String, String])
object MemeAPIRequest {
  implicit val rw: ReadWriter[MemeAPIRequest] = macroRW

  /**
   * 构建请求体
   *
   * @return 请求体
   */
  def apply(id: Int, sentences: List[String]): String = write(
    new MemeAPIRequest(id, sentences.zipWithIndex.map {
      case (sentence, idx) => s"sentence$idx" -> sentence
    }.toMap)
  )

}
