package o.lartifa.jam.common.config

import com.typesafe.config.Config

/**
 * 果酱插件设置
 *
 * Author: sinar
 * 2020/10/1 21:33
 */
object JamPluginConfig {
  private val config: Config = configFile.getConfig("plugin")

  // 是否自动启用插件
  val autoEnablePlugins: Boolean = config.getBoolean("auto_enable")

  // 插件路径
  val path: String = config.getString("plugin_path")
}
