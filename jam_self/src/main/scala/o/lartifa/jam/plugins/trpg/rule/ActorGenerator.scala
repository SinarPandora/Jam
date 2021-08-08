package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.rule.Attr.AttrApply

/**
 * 角色生成器
 *
 * Author: sinar
 * 2021/8/8 20:18
 */
case class ActorGenerator(rate: Int, attrApplies: Seq[AttrApply])
