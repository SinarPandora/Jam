package jam.plugins.meme_maker.engine

import o.lartifa.jam.common.config.JSONConfig.formats
import org.json4s.jackson.Serialization.write

/**
 * API 请求生成器
 *
 * Author: sinar
 * 2020/11/18 22:25
 */
case class MemeAPIRequest(id: Long, fillings: Map[String, String])
object MemeAPIRequest {

  /**
   * 构建请求体
   *
   * @return 请求体
   */
  def apply(id: Long, sentences: List[String]): String = write(
    new MemeAPIRequest(id, sentences.zipWithIndex.map {
      case (sentence, idx) => s"sentence$idx" -> sentence
    }.toMap)
  )

}
