package jam.plugins.meme_maker.engine

import java.time.LocalDateTime
import java.util.Base64
import java.util.concurrent.{SynchronousQueue, ThreadPoolExecutor, TimeUnit}

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import jam.plugins.meme_maker.engine.MemeAPIResponse._
import o.lartifa.jam.pool.JamContext
import upickle.default._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.{Failure, Success, Try}

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
  def generate(id: Long, sentences: List[String]): Try[ComponentImageBase64] = Try {
    val step1Resp = requests.post(generateApi, data = MemeAPIRequest(id, sentences)).text()
    val picUrl = domain + read[Response[PicData]](step1Resp).body.url
    logger.log(s"Meme Gif 已生成：$picUrl")
    val base64Data = Base64.getEncoder.encodeToString(requests.get(picUrl, readTimeout = 10000).bytes)
    new ComponentImageBase64(base64Data)
  }.recoverWith(err => {
    logger.error(s"Gif 生成失败，模板 id 为$id", err)
    Failure(err)
  })

  /**
   * 通过模板 id 获取模板信息
   *
   * @param id 模板 id
   * @return 模板信息
   */
  def getTemplateSteps(id: Long): Try[TemplateInfo] = Try {
    read[Response[TemplateInfo]] {
      requests.post(templateInfoApi, data = Map("id" -> id.toString)).text()
    }.body
  }.recoverWith(err => {
    logger.error(s"无法获取模板信息，id 为：$id", err)
    Failure(err)
  })

  /**
   * 获取全部可用模板
   *
   * @return 模板列表
   */
  def allTemplates: List[TemplateInfo] = MemeAPICache.allTemplates

  /**
   * 初始化
   */
  def init(): Unit = {
    MemeAPICache.refreshTemplates()
  }


  private object MemeAPICache {

    private var cache: List[TemplateInfo] = Nil
    private var lastUpdate: LocalDateTime = LocalDateTime.now()
    private var isUpdating: Boolean = false
    private implicit val singlePool: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(
      new ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        new SynchronousQueue[Runnable]())
    )

    /**
     * 获取全部可用模板
     *
     * @return 模板列表
     */
    def allTemplates: List[TemplateInfo] = {
      keepTemplateUpdateToDate()
      cache
    }

    /**
     * 保持模板最新
     */
    private def keepTemplateUpdateToDate(): Unit = {
      // 每一小时更新一次模板
      if ((cache.isEmpty || lastUpdate.isBefore(LocalDateTime.now().minusHours(1))) && !isUpdating)
        synchronized {
          // 防止在进入方法的瞬间 flag 变更
          if (!isUpdating) {
            isUpdating = true
            Future {
              refreshTemplates()
              isUpdating = false
            }
          }
        }
    }

    /**
     * 刷新全部可用模板
     *
     * @return 模板列表
     */
    def refreshTemplates(): Unit = {
      val list = Try {
        read[Response[List[TemplateInfo]]](requests.get(templateListApi).text())
          .body.sortBy(_.id)
      }.recoverWith(err => {
        logger.error("获取模板列表失败", err)
        Success(Nil)
      }).get
      if (list.nonEmpty) this.cache = list
      lastUpdate = LocalDateTime.now()
    }
  }

}
