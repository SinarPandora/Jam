package o.lartifa.jam.plugins.trpg.rule

import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.plugins.trpg.dice.ast.{DiceExpr, DiceSuit}
import o.lartifa.jam.plugins.trpg.rule.Attr.AttrApply
import o.lartifa.jam.plugins.trpg.rule.AttrAdjust.AdjustFunc
import o.lartifa.jam.plugins.trpg.rule.Check.{PercentCheckF, RangeCheck}
import o.lartifa.jam.plugins.trpg.rule.TRPGRule.SuccRules
import o.lartifa.jam.plugins.trpg.rule.TRPGRule.SuccRules.SuccRule

import scala.util.Try


/**
 * 规则解析器
 *
 * Author: sinar
 * 2021/7/25 23:12
 */
class RuleConfParser(val configName: String) {
  /**
   * 抛出异常
   *
   * @param parent 错误父级字段位置
   * @param reason 错误原因
   */
  @throws[ParseFailException]
  private def failToParse[T](parent: String, reason: String): T = {
    throw ParseFailException(s"规则：${configName}读取失败，原因为：$reason，错误位置：$parent")
  }

  /**
   * 转换配置文件到跑团规则
   *
   * @param conf 配置文件
   * @return TRPG 规则
   */
  @throws[ParseFailException]
  def parse(conf: TRPGRuleConf): TRPGRule = {
    val succRule = SuccRules.withName(conf.checking.successRule)
    val (succCheckers, failCheckers) = parseChecks(conf.checking, succRule)
    val (rate, actorGenerator) = parseActorGenerator(conf.actorGeneration)
    TRPGRule(
      name = conf.name,
      succCheckers = succCheckers,
      failCheckers = failCheckers,
      succRule = succRule,
      extraAttrs = parseExtraAttrs(conf.extraAttrs, rate),
      extraAdjusts = parseAttrAdjusts(conf.extraAdjusts),
      actorGenerator = actorGenerator
    )
  }

  type SuccessAndFailCheckers = (List[Either[PercentCheckF, RangeCheck]], List[Either[PercentCheckF, RangeCheck]])

  /**
   * 解析检定校验器
   * 成功校验顺序：大成功 -> 极难成功 -> 困难成功 -> 普通校验
   * 失败校验顺序：大失败 -> 普通校验
   *
   * @param conf 检定校验配置
   * @return 检定校验器组
   */
  private def parseChecks(conf: ChecksConf, succRule: SuccRule): SuccessAndFailCheckers = {
    (
      List(
        parseCheck("大成功", conf.hugeSuccess, succRule),
        parseCheck("极难成功", conf.veryHardSuccess, succRule),
        parseCheck("困难成功", conf.hardSuccess, succRule)
      ),
      List(
        parseCheck("大失败", conf.hugeFail, succRule)
      )
    )
  }

  /**
   * 解析校验
   *
   * @param name     校验名
   * @param conf     校验配置
   * @param succRule 成功规则
   * @return 解析结果
   */
  @throws[ParseFailException]
  private def parseCheck(name: String, conf: CheckConf, succRule: SuccRule): Either[PercentCheckF, RangeCheck] = {
    if (conf.range.contains("-")) {
      val range = Try(conf.range.split("-").map(_.toInt).toList)
        .getOrElse(failToParse("检定", s"${name}检定条件无效"))
      if (range.lengthIs != 2) {
        failToParse("检定", s"${name}检定条件无效")
      }
      val start :: end :: Nil = range
      Right(new RangeCheck(name, conf.prompt, start to end))
    } else {
      val percent = Try(conf.range.toDouble).getOrElse(
        failToParse("检定", s"${name}检定条件无效")
      )
      Left(new PercentCheckF(name, conf.prompt, percent, succRule))
    }
  }

  /**
   * 解析额外属性
   *
   * @param conf 额外属性配置
   * @param rate 属性倍率
   * @return 额外属性生成器组
   */
  @throws[ParseFailException]
  private def parseExtraAttrs(conf: Map[String, ActorAttr], rate: Int): Map[String, AttrApply] = {
    conf.map {
      case (name, conf) => Try(name -> parseAttr(name, conf, rate))
        .recover(err => failToParse("额外属性", err.getMessage))
        .get
    }
  }

  /**
   * 解析人物生成器
   *
   * @param conf 配置
   * @return 任务生成器
   */
  @throws[ParseFailException]
  private def parseActorGenerator(conf: ActorGeneration): (Int, ActorGenerator) = {
    val attrApplies = Try(conf.actorAttrs.map(it => parseAttr(it.name, it, conf.ratio)))
      .recover(err => failToParse("人物生成/属性", err.getMessage))
      .get
    conf.ratio -> ActorGenerator(attrApplies)
  }

  /**
   * 解析属性调整
   *
   * @param conf 配置
   * @return 属性调整
   */
  @throws[ParseFailException]
  private def parseAttrAdjusts(conf: List[ExtraAdjust]): Map[String, AdjustFunc] = {
    conf.map(conf => {
      val adjFunc = Try(AttrAdjust.parse(conf.adjustExpr))
        .recover(_ => failToParse("属性调整", s"调整表达式不正确，属性名：${conf.attr}")).get
        .getOrElse(failToParse("属性调整", s"调整表达式不正确，属性名：${conf.attr}"))
      val rangeOpt = conf.range.map(str => {
        val params = Try(str.split("-").map(_.toInt).toList)
          .getOrElse(throw ParseFailException(s"范围解析失败，属性名：${conf.attr}"))
        if (params.sizeIs == 2) {
          val start :: end :: Nil = params
          start to end
        } else throw ParseFailException(s"范围解析失败，属性名：${conf.attr}")
      })
      val func = rangeOpt match {
        case Some(range) => (attr: Attr) => {
          val value = adjFunc(attr)
          if (value.value < range.start) value.copy(value = range.start)
          else if (value.value > range.end) value.copy(value = range.end)
          else value
        }
        case None => adjFunc
      }
      conf.attr -> func
    }).toMap
  }

  /**
   * 解析单个属性
   *
   * @param name 属性名
   * @param conf 属性配置
   * @param rate 属性倍率
   * @return 属性生成函数
   */
  @throws[ParseFailException]
  private def parseAttr(name: String, conf: ActorAttr, rate: Int): AttrApply = {
    val expr = Try(DiceExpr(conf.valueExpr))
      .recover(err => throw ParseFailException(s"属性${name}的默认值表达式解析失败：${err.getMessage}"))
      .get
    val rangeOpt = conf.range.map(str => {
      val params = Try(str.split("-").map(_.toInt).toList)
        .getOrElse(throw ParseFailException(s"属性${name}的范围解析失败"))
      if (params.sizeIs == 2) {
        val start :: end :: Nil = params
        start to end
      } else throw ParseFailException(s"属性${name}的范围解析失败")
    })
    val func = rangeOpt match {
      case Some(range) =>
        (ds: DiceSuit) => {
          val value = expr.tryEval(ds).value * rate
          if (value < range.start) range.start
          else if (value > range.end) range.end
          else value
        }
      case None => (ds: DiceSuit) => {
        expr.tryEval(ds).value * rate
      }
    }
    (ds: DiceSuit) => {
      Attr(name = name, value = func(ds), hidden = conf.hidden.getOrElse(false))
    }
  }
}
