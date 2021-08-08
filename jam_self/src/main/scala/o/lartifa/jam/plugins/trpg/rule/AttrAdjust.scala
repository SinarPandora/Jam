package o.lartifa.jam.plugins.trpg.rule

import scala.util.matching.Regex

/**
 * 属性调整
 *
 * Author: sinar
 * 2021/7/24 23:27
 */
object AttrAdjust {
  /**
   * 调整函数
   */
  type AdjustFunc = Attr => Attr

  // 调整字符串模式
  private val pattern: Regex = """([+\-*xX/=?!？！])\W*([0-9]+)""".r("opt", "value")

  /**
   * 解析为调整函数
   *
   * @param adjustStr 调整字符串
   * @return 调整函数
   */
  def parse(adjustStr: String): Option[AdjustFunc] = {
    pattern.findFirstMatchIn(adjustStr).map(result => {
      val opt = result.group("opt")
      val value = result.group("value").toInt
      opt match {
        case "+" => i => i.copy(value = i.value + value)
        case "-" => i => i.copy(value = i.value - value)
        case "*" | "x" | "X" => i => i.copy(value = i.value * value)
        case "/" => i => i.copy(value = i.value / value)
        case "=" => i => i.copy(value = value)
        case "？" | "?" => i => i.copy(hidden = true)
        case "！" | "!" => i => i.copy(hidden = false)
      }
    })
  }

}
