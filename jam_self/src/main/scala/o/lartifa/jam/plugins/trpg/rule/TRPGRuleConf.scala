package o.lartifa.jam.plugins.trpg.rule

/**
 * TRPG 配置
 *
 * Author: sinar
 * 2021/7/25 23:09
 */
case class TRPGRuleConf
(
  name: String,
  checking: CheckersConf,
  extraAttrs: Map[String, Int],
  extraAdjusts: List[ExtraAdjust],
  actorGeneration: ActorGeneration
)
