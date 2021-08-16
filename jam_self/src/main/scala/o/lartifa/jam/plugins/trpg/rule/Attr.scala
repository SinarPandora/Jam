package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.dice.ast.DiceSuit
import ujson.Value.Value
import upickle.default
import upickle.default._

/**
 * 调查员属性
 *
 * Author: sinar
 * 2021/7/25 11:39
 */
case class Attr(name: String, value: Int, hidden: Boolean)

object Attr {
  type AttrApply = DiceSuit => Attr
  /**
   * 属性组
   */
  type Attrs = Map[String, Attr]

  /**
   * 创建空的属性组
   *
   * @return 空属性组
   */
  def empty: Attrs = Map[String, Attr]()

  implicit val attrRW: default.ReadWriter[Attr] = macroRW[Attr]

  /**
   * 将属性组转换为 JSON
   *
   * @param attrs 属性组
   * @return JSON 字符串
   */
  def attrsToJson(attrs: Attrs): Value = write(attrs)

  /**
   * 将 JSON 转换为属性组
   *
   * @param value 属性组
   * @return 属性组
   */
  def jsonToAttrs(value: Value): Attrs = {
    value.obj.map {
      case (key, value) => key -> Attr(
        name = key,
        value = value.obj.get("value").map(_.num.toInt).getOrElse(0),
        hidden = value.obj.get("hidden").exists(_.bool)
      )
    }.toMap
  }
}
