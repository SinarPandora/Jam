package o.lartifa.jam.common

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Author: sinar
 * 2021/5/9 20:20
 */
package object config {
  val configFile: Config = ConfigFactory.load()
}
