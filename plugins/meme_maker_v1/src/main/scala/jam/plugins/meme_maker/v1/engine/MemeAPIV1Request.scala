package jam.plugins.meme_maker.v1.engine

import upickle.default._

/**
 * API 请求生成器
 *
 * Author: sinar
 * 2020/11/18 22:25
 */
object MemeAPIV1Request {
  /**
   * 构建请求体
   *
   * @return 请求体
   */
  def apply(sentences: List[String]): String = write(
    sentences.zipWithIndex.map {
      case (sentence, idx) => s"$idx" -> sentence
    }.toMap
  )

}
