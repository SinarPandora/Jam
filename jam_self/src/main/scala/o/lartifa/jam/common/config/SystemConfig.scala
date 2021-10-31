package o.lartifa.jam.common.config

import com.typesafe.config.Config

import scala.jdk.CollectionConverters.*

/**
 * 系统配置
 *
 * Author: sinar
 * 2020/1/4 22:46
 */
object SystemConfig {
  private val config: Config = botConfigFile.getConfig("system")

  // SXDL 脚本目录
  val sxdlPath: String = config.getString("sxdl_path")
  // SXDL 脚本扩展名列表
  val sxdlFileExtension: List[String] = config.getStringList("file_extension").asScala.toList
  // Debug 模式开启标识
  val debugMode: Boolean = config.getBoolean("debugMode")
  // 自动清理消息天数
  val cleanUpMessagePeriod: Int = config.getInt("auto_remove_message_before")
  // 临时文件目录
  val tempDir: String = config.getString("temp_dir")
}
