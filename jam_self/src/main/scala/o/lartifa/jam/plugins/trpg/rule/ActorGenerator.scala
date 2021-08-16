package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.dice.ast.DiceSuit
import o.lartifa.jam.plugins.trpg.rule.Attr.{AttrApply, Attrs}

/**
 * 角色生成器
 *
 * Author: sinar
 * 2021/8/8 20:18
 */
case class ActorGenerator(attrApplies: Seq[AttrApply]) {
  /**
   * 生成角色属性
   *
   * @param ds 骰子组
   * @return 属性
   */
  def generate(ds: DiceSuit): Attrs = {
    Map[String, Attr]() ++ attrApplies.map(_.apply(ds)).map {
      case attr@Attr(name, _, _) => name -> attr
    }
  }
}
