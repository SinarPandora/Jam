package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * 果酱配置
 *
 * Author: sinar
 * 2020/1/2 22:01 
 */
object JamConfig {
  private val config: Config = ConfigFactory.load().getConfig("bot")

  val name: String = config.getString("name")

  val masterQID: Long = config.getLong("master_qq")
}
