package jam.plugins.meme_maker.engine

import java.util.Base64

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import jam.plugins.meme_maker.engine.MemeAPIResponse._
import o.lartifa.jam.pool.JamContext
import upickle.default._

import scala.util.{Failure, Try}

/**
 * Meme maker
 *
 * powered by: https://app.xuty.tk/
 * Author: sinar
 * 2020/11/18 22:31
 */
object MemeMaker {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(MemeMaker.getClass)
  val domain: String = "https://app.xuty.tk"
  val api: String = s"$domain/memeet/api/v1/template/make"

  /**
   * 生成 Gif
   *
   * @param id        模板 ID
   * @param sentences 填充句集合
   * @return 生成结果
   */
  def generate(id: Int, sentences: List[String]): Option[ComponentImageBase64] = Try {
    val step1Resp = requests.post(api, data = MemeAPIRequest(id, sentences)).text()
    val picUrl = domain + read[Response](step1Resp).body.url
    val base64Data = Base64.getEncoder.encodeToString(requests.get(picUrl, readTimeout = 10000).bytes)
    new ComponentImageBase64(base64Data)
  }.recoverWith(err => {
    logger.error(err)
    Failure(err)
  }).toOption
}
