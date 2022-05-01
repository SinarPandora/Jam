package o.lartifa.jam.common.config

import org.json4s.{DefaultFormats, Formats}

/**
 * JSON4s 配置
 *
 * Author: sinar
 * 2022/4/30 22:21
 */
object JSONConfig {
  implicit val formats: Formats = DefaultFormats
}
