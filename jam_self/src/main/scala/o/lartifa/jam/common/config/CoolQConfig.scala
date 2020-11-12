package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

/**
 * 酷 Q 连接设置
 *
 * Author: sinar
 * 2019/9/28 15:19
 */
object CoolQConfig {
  private val config: Config = ConfigFactory.load().getConfig("coolQQ")

  // 酷 Q 服务器地址
  val postUrl: String = config.getString("host")
  // 消息推送端口
  val postPort: Int = config.getInt("port.post")
  // 消息接收窗口
  val socketPort: Int = config.getInt("port.socket")

  object Backend {
    private val config: Config = CoolQConfig.config.getConfig("backend")
    object Mirai {
      private val config: Config = Backend.config.getConfig("mirai")
      // Mirai 后端路径
      val path: String = config.getString("path")
    }
  }
}
