package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * 果酱插件设置
 *
 * Author: sinar
 * 2020/10/1 21:33
 */
object JamPluginConfig {
  private val config: Config = ConfigFactory.load().getConfig("plugin")

  // 是否自动启用插件
  val autoEnablePlugins: Boolean = config.getBoolean("auto_enable")
}
