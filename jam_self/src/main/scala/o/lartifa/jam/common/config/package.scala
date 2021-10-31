package o.lartifa.jam.common

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Author: sinar
 * 2021/5/9 20:20
 */
package object config {
  val botConfigFile: Config = ConfigFactory.load()

  trait Reloadable {
    /**
     * 重新加载
     */
    def reload(): Unit
  }
}
