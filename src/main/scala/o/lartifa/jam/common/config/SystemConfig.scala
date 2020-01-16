package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.jdk.CollectionConverters._

/**
 * 系统配置
 *
 * Author: sinar
 * 2020/1/4 22:46 
 */
object SystemConfig {
  private val config: Config = ConfigFactory.load().getConfig("system")

  // SSDL 文件目录
  val ssdlPath: String = config.getString("ssdl_path")
  // SSDL 文件扩展名列表
  val ssdlFileExtension: List[String] = config.getStringList("file_extension").asScala.toList
  // Debug 模式开启标识
  val debugMode: Boolean = config.getBoolean("debugMode")
}
