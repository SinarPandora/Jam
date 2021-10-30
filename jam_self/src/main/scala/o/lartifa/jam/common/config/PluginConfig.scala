package o.lartifa.jam.common.config

import com.typesafe.config.Config
import o.lartifa.jam.common.config.PluginConfig.DreamAI
import o.lartifa.jam.common.exception.ParseFailException

/**
 * Author: sinar
 * 2021/10/25 00:08
 */
case class PluginConfig
(
  dreamAI: DreamAI
)

object PluginConfig extends Reloadable {
  case class DreamAI(mobile: String)

  private var _config: Option[PluginConfig] = None

  def config: PluginConfig = _config.getOrElse(throw ParseFailException("在配置尚未初始化前获取了其内容，是 BUG，请上报"))

  /**
   * 重新加载
   */
  override def reload(): Unit = {
    val config: Config = DynamicConfigLoader.config
    this._config = Some(
      PluginConfig(
        dreamAI = DreamAI(
          mobile = config.getString("dream_ai.mobile").trim
        )
      ))
  }
}
