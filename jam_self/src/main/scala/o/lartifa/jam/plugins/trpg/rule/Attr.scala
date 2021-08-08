package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.dice.ast.DiceSuit
import ujson.Value.Value

import scala.collection.mutable

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
  type Attrs = mutable.Map[String, Attr]

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
    }
  }
}
