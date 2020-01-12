package o.lartifa.jam.common.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.jdk.CollectionConverters._

/**
 * Author: sinar
 * 2020/1/4 22:46 
 */
object SystemConfig {
  private val config: Config = ConfigFactory.load().getConfig("system")

  val ssdlPath: String = config.getString("ssdl_path")
  val ssdlFileExtension: List[String] = config.getStringList("file_extension").asScala.toList
  val debugMode: Boolean = config.getBoolean("debugMode")
}
