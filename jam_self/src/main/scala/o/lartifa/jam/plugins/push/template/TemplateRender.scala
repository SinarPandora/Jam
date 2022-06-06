package o.lartifa.jam.plugins.push.template

import better.files.*
import cc.moecraft.icq.sender.message.components.ComponentImage
import cc.moecraft.logger.HyLogger
import cn.hutool.extra.template.{TemplateConfig, TemplateEngine, TemplateUtil}
import o.lartifa.jam.common.config.{PluginConfig, SystemConfig}
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.plugins.push.source.SourceIdentity
import o.lartifa.jam.pool.JamContext

import scala.jdk.CollectionConverters.*
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
  def templates: Map[String, String] = PluginConfig.config.sourcePush.templateMapping

  /**
   * @return 模板文件夹
   */
  def templateDir: File = PluginConfig.config.sourcePush.templateDir.toFile

  // 渲染工具
  private val renderApp: String = "npx --registry=https://registry.npmmirror.com --yes node-html-to-image-cli"
  // 临时文件夹
  private lazy val tmp: File = (File(SystemConfig.tempDir) / "source_push").createDirectoryIfNotExists()
  // 模板引擎
  private lazy val engine: TemplateEngine = TemplateUtil.createEngine(new TemplateConfig())
  // 执行日志
  private val processLogger: ProcessLogger = ProcessLogger(logger.log, logger.error)

  /**
   * 模板渲染
   *
   * @param data       模板数据
   * @param source     订阅源
   * @param messageKey 消息唯一标识
   * @return 渲染结果
   */
  def render(data: Map[String, Any], source: SourceIdentity, messageKey: String): Try[RenderResult] = {
    val tmpDir = (tmp / s"${source.sourceType}_${source.sourceIdentity}").createDirectoryIfNotExists()
    val workDir = (tmpDir / messageKey).createDirectoryIfNotExists()
    val renderImage = workDir / "rendered.png"
    if (renderImage.exists) {
      Success(RenderResult(new ComponentImage(renderImage.pathAsString).toString))
    } else templates.get(source.sourceType) match {
      case Some(name) =>
        Try {
          val templatePath = s"$templateDir/$name.ftl"
          val template = engine.getTemplate(templatePath)
          Using((workDir / "source.html").newBufferedWriter) { writer =>
            template.render(data.asJava, writer)
            writer.flush()
          }
          val cmd =
            s"""cd ${workDir.pathAsString}
               |$renderApp source.html rendered.png""".stripMargin
          val rtnCode: Int = cmd.!(processLogger)
          if (rtnCode != 0) throw ExecutionException(s"渲染失败，请检查日志信息，订阅源：$source")
          else RenderResult(new ComponentImage(renderImage.pathAsString).toString)
        }
      case None => Failure(ExecutionException(s"模板未注册，订阅源：$source"))
    }
  }
}
