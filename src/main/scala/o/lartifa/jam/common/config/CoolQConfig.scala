package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Author: sinar
 * 2019/9/28 15:19
 */
object CoolQConfig {
  private val config: Config = ConfigFactory.load().getConfig("coolQQ")

  val postUrl: String = config.getString("host")
  val postPort: Int = config.getInt("port.post")
  val socketPort: Int = config.getInt("port.socket")
}
