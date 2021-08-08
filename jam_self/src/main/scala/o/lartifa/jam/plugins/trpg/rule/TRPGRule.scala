package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.plugins.trpg.rule.Attr.AttrApply
import o.lartifa.jam.plugins.trpg.rule.AttrAdjust.AdjustFunc
import o.lartifa.jam.plugins.trpg.rule.Check.{PercentCheckF, RangeCheck}
import o.lartifa.jam.plugins.trpg.rule.TRPGRule.SuccRules.SuccRule

/**
 * TRPG 规则
 *
 * Author: sinar
 * 2021/7/24 21:28
 */
case class TRPGRule
(
  name: String,
  succCheckers: List[Either[PercentCheckF, RangeCheck]],
  failCheckers: List[Either[PercentCheckF, RangeCheck]],
  succRule: SuccRule,
  extraAttrs: Map[String, AttrApply],
  extraAdjusts: Map[String, AdjustFunc],
  actorGenerator: ActorGenerator
)

object TRPGRule {
  object SuccRules extends Enumeration {
    type SuccRule = Value
    val Gt: SuccRules.Value = Value("大于")
    val Ge: SuccRules.Value = Value("大于等于")
    val Lt: SuccRules.Value = Value("小于")
    val Le: SuccRules.Value = Value("小于等于")
  }
}
