package o.lartifa.jam.common.config

import com.typesafe.config.Config
import o.lartifa.jam.common.util.BetterConfig.*

/**
 * 果酱配置
 *
 * Author: sinar
 * 2020/1/2 22:01
 */
object BotConfig {
  val config: Config = botConfigFile.getConfig("bot")

  // Bot QQ
  val qID: Long = config.getLong("jam_qq")

  // Bot password
  val password: String = config.getString("password")

  // 果酱配置文件存放位置
  val jamConfigFile: String = config.getString("jam_config_file", "../conf/sxdl")

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
    // 分支名称
    val branch: String = remote.getString("branch")
  }
}
