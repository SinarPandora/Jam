package o.lartifa.jam.common.config

import com.typesafe.config.Config
import o.lartifa.jam.common.config.PluginConfig.*
import o.lartifa.jam.common.exception.ParseFailException

import scala.jdk.CollectionConverters.*

/**
 * Author: sinar
 * 2021/10/25 00:08
 */
case class PluginConfig
(
  dreamAI: DreamAI,
  picBot: PicBot,
  rss: Rss,
  preHandle: PreHandle,
  postHandle: PostHandle
)

object PluginConfig extends Reloadable {
  case class DreamAI(mobile: String)
  case class PicBot(apiKey: String)
  case class Rss(deployUrl: String, defaultStyle: String, customStyles: Map[String, String])
  case class FlipRepeatPicture(useFFMpeg: Boolean, ffmpegPath: String)
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
    val config: Config = DynamicConfigLoader.config.getConfig("plugin")
    this._config = Some(
      PluginConfig(
        dreamAI = DreamAI(mobile = config.getString("dream_ai.mobile")),
        picBot = PicBot(apiKey = config.getString("picbot.apikey")),
        rss = Rss(
          deployUrl = config.getString("rss.deploy_url"),
          defaultStyle = config.getString("rss.style.default"),
          customStyles = config.getConfig("rss.style.custom")
            .entrySet().asScala
            .map(it => it.getKey.replace("\"", "") ->
              it.getValue.render().replace("\"", ""))
            .toMap
        ),
        preHandle = PreHandle(
          runTaskAsync = config.getBoolean("pre_handle.run_task_async"),
          enabledTasks = config.getStringList("pre_handle.enabled_tasks").asScala.toList,
          flipRepeatPicture = FlipRepeatPicture(
            useFFMpeg = config.getBoolean("pre_handle.flip_repeat_picture.use_ffmpeg"),
            ffmpegPath = config.getString("pre_handle.flip_repeat_picture.ffmpeg_path")
          )
        ),
        postHandle = PostHandle(
          runTaskAsync = config.getBoolean("post_handle.run_task_async"),
          enabledTasks = config.getStringList("post_handle.enabled_tasks").asScala.toList
        )
      ))
  }
}
