package jam.plugins.meme_maker.v1.engine

import upickle.default._

/**
 * API 请求生成器
 *
 * Author: sinar
 * 2020/11/18 22:25
 */
case class MemeAPIV1Request(id: Long, fillings: Map[String, String])
object MemeAPIV1Request {
  implicit val rw: ReadWriter[MemeAPIV1Request] = macroRW

  /**
   * 构建请求体
   *
   * @return 请求体
   */
  def apply(id: Long, sentences: List[String]): String = write(
    new MemeAPIV1Request(id, sentences.zipWithIndex.map {
      case (sentence, idx) => s"sentence$idx" -> sentence
    }.toMap)
  )

}
