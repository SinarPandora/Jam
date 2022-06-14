package o.lartifa.jam.plugins.push.template

import better.files.*
import cc.moecraft.icq.sender.message.components.ComponentImage
import cc.moecraft.logger.HyLogger
import freemarker.template.Configuration
import o.lartifa.jam.common.config.{PluginConfig, SystemConfig}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.pool.JamContext

import java.nio.charset.StandardCharsets
import java.util
import scala.sys.process.*
import scala.util.{Failure, Success, Try, Using}


/**
 * 模板渲染器
 *
 * Author: sinar
 * 2022/6/5 20:32
 */
object TemplateRender {
  protected val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)

  /**
   * @return 全部模板配置
   */
  private def templates: Map[String, String] = PluginConfig.config.sourcePush.templateMapping

  /**
   * @return 模板文件夹
   */
  private def templateDir: File = PluginConfig.config.sourcePush.templateDir.toFile

  // 渲染工具
  private val renderApp: String = "node-html-to-image-cli"

  // 临时文件夹
  private lazy val tmp: File = (File(SystemConfig.tempDir) / "source_push").createDirectoryIfNotExists()
  // 模板引擎
  private lazy val engine: Configuration = {
    val cfg = new Configuration(Configuration.VERSION_2_3_31)
    cfg.setDirectoryForTemplateLoading(templateDir.toJava)
    cfg.setDefaultEncoding(StandardCharsets.UTF_8.name())
    cfg
  }
  // 执行日志
  private val processLogger: ProcessLogger = ProcessLogger(logger.log, logger.error)

  /**
   * 通过文件是否存在判断是否渲染过
   *
   * @param source     订阅源
   * @param messageKey 消息唯一标识
   * @return 是否渲染过
   */
  def isRendered(source: SourceIdentity, messageKey: String): Boolean =
    (tmp / s"${source.sourceType}_${source.sourceIdentity}" / messageKey / "rendered.png").exists

  /**
   * 模板渲染
   *
   * @param source     订阅源
   * @param messageKey 消息唯一标识
   * @param data       模板数据
   * @return 渲染结果
   */
  def render(source: SourceIdentity, messageKey: String, data: util.Map[String, Object] = new util.HashMap()): Try[RenderResult] = {
    val tmpDir = (tmp / s"${source.sourceType}_${source.sourceIdentity}").createDirectoryIfNotExists()
    val workDir = (tmpDir / messageKey).createDirectoryIfNotExists()
    val renderImage = workDir / "rendered.png"
    if (renderImage.exists) {
      Success(RenderResult(new ComponentImage(s"file://${renderImage.pathAsString}").toString))
    } else templates.get(source.sourceType) match {
      case Some(name) =>
        Try {
          val templateName = s"$name.ftl"
          val template = engine.getTemplate(templateName)
          Using((workDir / "source.html").newBufferedWriter) { writer =>
            template.process(data, writer)
            writer.flush()
          }
          val cmd = Process(s"$renderApp source.html rendered.png",
            workDir.toJava,
            ("PUPPETEER_EXECUTABLE_PATH", PluginConfig.config.sourcePush.browserPath))
          cmd.!(processLogger)
          var count = 0
          while (count < 5) {
            if (renderImage.notExists) {
              Thread.sleep(3000)
              count += 1
            } else {
              return Success(RenderResult(new ComponentImage(s"file://${renderImage.pathAsString}").toString))
            }
          }
          throw ExecutionException(s"渲染失败，请检查日志信息，订阅源：$source")
        }
      case None => Failure(ExecutionException(s"模板未注册，订阅源：$source"))
    }
  }
}
