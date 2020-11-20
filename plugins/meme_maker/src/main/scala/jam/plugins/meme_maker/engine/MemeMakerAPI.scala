package jam.plugins.meme_maker.engine

import java.util.Base64

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import jam.plugins.meme_maker.engine.MemeAPIResponse._
import o.lartifa.jam.common.exception.ExecutionException
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
object MemeMakerAPI {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(MemeMakerAPI.getClass)
  val domain: String = "https://app.xuty.tk"
  val generateApi: String = s"$domain/memeet/api/v1/template/make"
  val templateInfoApi: String = s"$domain/memeet/api/v1/template/info"
  val templateListApi: String = s"$domain/memeet/api/v1/trending?offset=0&count=100"

  /**
   * 生成 Gif
   *
   * @param id        模板 ID
   * @param sentences 填充句集合
   * @return 生成结果
   */
  def generate(id: Int, sentences: List[String]): Try[ComponentImageBase64] = Try {
    val step1Resp = requests.post(generateApi, data = MemeAPIRequest(id, sentences)).text()
    val picUrl = domain + read[Response[PicData]](step1Resp).body.url
    logger.log(s"Meme Gif 已生成：$picUrl")
    val base64Data = Base64.getEncoder.encodeToString(requests.get(picUrl, readTimeout = 10000).bytes)
    new ComponentImageBase64(base64Data)
  }.recoverWith(err => {
    logger.error(err)
    Failure(ExecutionException(s"Gif 生成失败，模板 id 为$id"))
  })

  /**
   * 通过模板 id 获取模板信息
   *
   * @param id 模板 id
   * @return 模板信息
   */
  def getTemplateSteps(id: Int): Try[TemplateInfo] = Try {
    read[Response[TemplateInfo]] {
      requests.post(templateInfoApi, data = Map("id" -> id.toString)).text()
    }.body
  }.recoverWith(err => {
    logger.error(err)
    Failure(ExecutionException(s"无法获取模板信息，id 为：$id"))
  })

  /**
   * 获取全部可用模板
   *
   * @return 模板列表
   */
  def allTemplates: Try[List[TemplateInfo]] = Try {
    read[Response[List[TemplateInfo]]](requests.get(templateListApi).text()).body
  }.recoverWith(err => {
    logger.error(err)
    Failure(ExecutionException("获取模板列表失败"))
  })
}
