package jam.plugins.meme_maker.v1.engine

import java.time.LocalDateTime
import java.util.Base64
import java.util.concurrent.{SynchronousQueue, ThreadPoolExecutor, TimeUnit}

import cc.moecraft.icq.sender.message.components.ComponentImageBase64
import cc.moecraft.logger.HyLogger
import jam.plugins.meme_maker.v1.engine.MemeAPIV1Response._
import o.lartifa.jam.pool.JamContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.jdk.CollectionConverters._
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
  val domain: String = "https://sorry.xuty.cc"

  def templateInfoApi(code: String): String = s"https://sorry.xuty.cc/$code/"

  def generateApi(code: String): String = s"$domain/$code/make"

  val templateListPage: String = s"$domain/wangjingze/"

  /**
   * 生成 Gif
   *
   * @param code      模板 Code
   * @param sentences 填充句集合
   * @return 生成结果
   */
  def generate(code: String, sentences: List[String]): Try[ComponentImageBase64] = Try {
    val step1Resp = requests.post(
      url = generateApi(code),
      headers = Map("content-type" -> "application/json;charset=UTF-8"),
      data = MemeAPIV1Request(sentences)
    ).text()
    val picUrl = domain + "/" + Jsoup.parse(step1Resp).getElementsByTag("a")
      .first().attr("href").stripPrefix("/")
    logger.log(s"Meme Gif 已生成：$picUrl")
    val base64Data = Base64.getEncoder.encodeToString(requests.get(picUrl).bytes)
    new ComponentImageBase64(base64Data)
  }.recoverWith(err => {
    logger.error(s"Gif 生成失败，模板 code 为$code", err)
    Failure(err)
  })

  /**
   * 通过模板 Code 获取模板槽
   *
   * @param code 模板 Code
   * @return 模板信息
   */
  def getTemplateSlots(code: String): Try[List[String]] = Try {
    val document: Document = Jsoup.connect(templateInfoApi(code)).get()
    document
      .select("input.w3-input.w3-border")
      .asScala
      .map(_.attr("placeholder"))
      .toList
  }.recoverWith(err => {
    logger.error(s"无法获取模板信息，link 为：${templateInfoApi(code)}", err)
    Failure(err)
  })

  /**
   * 获取全部可用模板
   *
   * @return 模板列表
   */
  def allTemplates: List[TemplatePair] = MemeAPICache.allTemplates

  /**
   * 初始化
   */
  def init(): Unit = {
    MemeAPICache.refreshTemplates()
  }


  private object MemeAPICache {

    private var cache: List[TemplatePair] = Nil
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
    def allTemplates: List[TemplatePair] = {
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
        val document = Jsoup.connect(templateListPage).get()
        document.select("a.w3-bar-item.w3-button")
          .asScala
          .filterNot(it => it.attr("href").contains("SORRY")
            || it.attr("href").contains("wenzhen"))
          .zipWithIndex
          .map {
            case (it, idx) => TemplatePair(idx + 1, it.text(),
              it.attr("href").stripPrefix("/").stripSuffix("/"))
          }.toList
      }.recoverWith(err => {
        logger.error("获取模板列表失败", err)
        Success(Nil)
      }).get
      if (list.nonEmpty) this.cache = list
      lastUpdate = LocalDateTime.now()
    }
  }

}
