package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * 果酱配置
 *
 * Author: sinar
 * 2020/1/2 22:01 
 */
object JamConfig {
  val config: Config = ConfigFactory.load().getConfig("bot")

  // Bot 姓名
  val name: String = config.getString("name")

  // Master 用户
  val masterQID: Long = config.getLong("master_qq")

  // 响应频率
  val responseFrequency: Int = config.getInt("response_frequency")
}
