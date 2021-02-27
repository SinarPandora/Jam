package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

import java.lang
import scala.jdk.CollectionConverters._

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

  // Bot QQ
  val qID: Long = config.getLong("jam_qq")

  // Bot password
  val password: String = config.getString("password")

  // 监护者列表
  val masterList: List[lang.Long] = config.getLongList("master_list").asScala.toList

  // 响应频率
  val responseFrequency: Int = config.getInt("response_frequency")

  // 自动接受好友申请
  val autoAcceptFriendRequest: Boolean = config.getBoolean("auto_accept_friend_request")

  // 自动接收群申请
  val autoAcceptGroupRequest: Boolean = config.getBoolean("auto_accept_group_request")

  // 是否让关键词匹配乱序执行
  val matchOutOfOrder: Boolean = config.getBoolean("match_out_of_order")

  // 远程编辑
  object RemoteEditing {
    private val remote: Config = config.getConfig("remote_editing")
    // 开启远程编辑
    val enable: Boolean = remote.getBoolean("enable")
    // 远程仓库地址（http）
    val repo: String = remote.getString("repo")
    // 用户名
    val username: String = remote.getString("user_name")
    // Git 邮件地址
    val email: String = remote.getString("git_email")
    // 秘钥
    val secret: String = remote.getString("secret")
  }
}
