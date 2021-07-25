package o.lartifa.jam.plugins.trpg.rule

/**
 * 对规则的配置文件建模
 *
 * Author: sinar
 * 2021/7/25 00:26
 */
case class CheckersConf
(
  successRule: String,
  hugeSuccess: CheckConf,
  hugeFail: CheckConf,
  hardSuccess: CheckConf,
  veryHardSuccess: CheckConf,
)

case class CheckConf
(
  range: String,
  prompt: Option[CheckPrompt]
)

case class CheckPrompt
(
  prob: Double,
  msgs: List[String]
)

case class ExtraAttr
(
  name: String,
  default: String,
  hidden: Option[Boolean],
  range: Option[String],
)

case class ExtraAdjust
(
  attr: String,
  adjust: String,
  range: Option[String]
)

case class ActorGeneration
(
  ratio: Int,
  actorAttrs: Seq[ActorAttr]
)

case class ActorAttr
(
  valueExpr: String,
  range: String,
  hidden: Option[Boolean]
)

