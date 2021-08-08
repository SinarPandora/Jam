package o.lartifa.jam.plugins.trpg.rule

/**
 * 对规则的配置文件建模
 *
 * Author: sinar
 * 2021/7/25 00:26
 */
case class ChecksConf
(
  successRule: String,
  hugeSuccess: CheckConf,
  hugeFail: CheckConf,
  hardSuccess: CheckConf,
  veryHardSuccess: CheckConf
)

case class CheckConf
(
  range: String,
  prompt: Option[CheckPrompt]
)

case class CheckPrompt
(
  prob: Double,
  msgs: Seq[String]
)

case class ExtraAdjust
(
  attr: String,
  adjustExpr: String,
  range: Option[String]
)

case class ActorGeneration
(
  ratio: Int,
  actorAttrs: Seq[ActorAttr]
)

case class ActorAttr
(
  name: String,
  valueExpr: String,
  range: Option[String],
  hidden: Option[Boolean]
)

