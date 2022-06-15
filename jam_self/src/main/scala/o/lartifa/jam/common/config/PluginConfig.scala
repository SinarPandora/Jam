package o.lartifa.jam.common.config

import com.typesafe.config.Config
import o.lartifa.jam.common.exception.ParseFailException

import scala.jdk.CollectionConverters.*

/**
 * 插件配置
 *
 * Author: sinar
 * 2021/10/25 00:08
 */
object PluginConfig extends Reloadable {
  case class DreamAI(mobile: String)
  case class PicBot(pixivProxy: String, apiBatchSize: Int)
  case class Rss(deployUrl: String, defaultStyle: String, customStyles: Map[String, String])
  case class FlipRepeatPicture(useFFMpeg: Boolean, ffmpegPath: String, useRandomFilter: Boolean)
  case class SourcePush
  (
    templateDir: String,
    browserPath: String,
    renderPath: String,
    scanFrequency: Int,
    templateMapping: Map[String, String]
  )
  case class PreHandle
  (
    runTaskAsync: Boolean,
    enabledTasks: List[String],
    flipRepeatPicture: FlipRepeatPicture
  )

  case class PostHandle
  (
    runTaskAsync: Boolean,
    enabledTasks: List[String]
  )

  private var _config: Option[PluginConfig] = None

  def config: PluginConfig = _config.getOrElse(throw ParseFailException("在配置尚未初始化前获取了其内容，是 BUG，请上报"))

  /**
   * 重新加载
   */
  override def reload(): Unit = {
    import o.lartifa.jam.common.util.BetterConfig.*
    val config: Config = DynamicConfigLoader.config.getConfig("plugin")
    this._config = Some(
      PluginConfig(
        dreamAI = DreamAI(mobile = config.getString("dream_ai.mobile")),
        picBot = PicBot(
          pixivProxy = config.getStringOrElse("picbot.pixiv_proxy", "i.pixiv.re"),
          apiBatchSize = config.getIntOrElse("picbot.api_batch_size", 100)
        ),
        rss = Rss(
          deployUrl = config.getStringOrElse("rss.deploy_url", ""),
          defaultStyle = config.getStringOrElse("rss.style.default", "图文混排"),
          customStyles = config.getConfig("rss.style.custom")
            .entrySet().asScala
            .map(it => it.getKey.replace("\"", "") ->
              it.getValue.render().replace("\"", ""))
            .toMap
        ),
        preHandle = PreHandle(
          runTaskAsync = config.getBooleanOrElse("pre_handle.run_task_async", default = true),
          enabledTasks = config.getStringListOrElse("pre_handle.enabled_tasks", List("反向复读图片", "替换小程序跳转")),
          flipRepeatPicture = FlipRepeatPicture(
            useFFMpeg = config.getBooleanOrElse("pre_handle.flip_repeat_picture.use_ffmpeg", default = false),
            ffmpegPath = config.getStringOrElse("pre_handle.flip_repeat_picture.ffmpeg_path", ""),
            useRandomFilter = config.getBooleanOrElse("pre_handle.flip_repeat_picture.use_random_filter", default = false)
          )
        ),
        postHandle = PostHandle(
          runTaskAsync = config.getBooleanOrElse("post_handle.run_task_async", default = true),
          enabledTasks = config.getStringListOrElse("post_handle.enabled_tasks", List())
        ),
        sourcePush = SourcePush(
          templateDir = config.getStringOrElse("source_push.template_dir", "../conf/sxdl/template"),
          browserPath = config.getStringOrElse("source_push.browser_path", "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe"),
          renderPath = config.getStringOrElse("source_push.render_path", "node-html-to-image-cli.cmd"),
          scanFrequency = config.getIntOrElse("source_push.scan_frequency", 3),
          templateMapping = config.getConfig("source_push.template_mapping")
            .entrySet().asScala
            .map(it => it.getKey.replace("\"", "") ->
              it.getValue.render().replace("\"", ""))
            .toMap
        )
      ))
  }
}

import o.lartifa.jam.common.config.PluginConfig.*

case class PluginConfig
(
  dreamAI: DreamAI,
  picBot: PicBot,
  rss: Rss,
  preHandle: PreHandle,
  postHandle: PostHandle,
  sourcePush: SourcePush
)
