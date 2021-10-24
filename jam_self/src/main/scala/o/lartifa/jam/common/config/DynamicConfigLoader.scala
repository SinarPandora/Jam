package o.lartifa.jam.common.config


import com.typesafe.config.{Config, ConfigFactory}
import o.lartifa.jam.common.exception.ParseFailException

import java.io.File


/**
 * 动态配置加载器
 *
 * Author: sinar
 * 2021/10/24 20:02
 */
object DynamicConfigLoader extends Reloadable {
  var _configFile: Option[Config] = None

  def config: Config = _configFile.getOrElse(throw ParseFailException("在配置尚未初始化前获取了其内容，是 BUG，请上报"))

  /**
   * 重新加载
   */
  override def reload(): Unit = {
    val configFile = new File(BotConfig.jamConfigFile)
    if (!configFile.exists()) {
      throw ParseFailException("配置文件不存在！")
    }
    this._configFile = Some(ConfigFactory.parseFile(configFile))
    JamConfig.reload()
  }
}
