package o.lartifa.jam.engine.parser

import ammonite.ops.PipeableImplicit
import o.lartifa.jam.model.conditions.{Condition, SenderCondition, SessionCondition, VarCondition}

/**
 * 解析条件结构
 *
 * Author: sinar
 * 2020/1/4 16:11
 */
object ConditionParser {

  import Patterns.ConditionPattern

  /**
   * 解析条件
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  def parseCondition(string: String)(implicit context: ParseEngineContext): Option[Condition] = {
    LazyList(parseParamCondition _, parseSenderCondition _, parseSessionCondition _)
      .map(_.apply(string))
      .find(_.isDefined)
      .flatten
  }

  /**
   * 解析变量条件
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseParamCondition(string: String)(implicit context: ParseEngineContext): Option[VarCondition] = {
    import VarCondition.Constant
    ConditionPattern.paramCondition.findFirstMatchIn(string).map(result => {
      val varKey = result.group("var") |> context.getVar
      val op = result.group("op") match {
        case Constant.gtOp => VarCondition.gtOp
        case Constant.geOp => VarCondition.geOp
        case Constant.ltOp => VarCondition.ltOp
        case Constant.leOp => VarCondition.leOp
        case Constant.eqOp => VarCondition.eqOp
        case Constant.neOp => VarCondition.neOp
      }
      val template = result.group("template") |> context.getTemplate
      VarCondition(varKey, op, template)
    })
  }

  /**
   * 解析发送者信息条件
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseSenderCondition(string: String)(implicit context: ParseEngineContext): Option[SenderCondition] = {
    import SenderCondition.Constant
    ConditionPattern.senderCondition.findFirstMatchIn(string).map(result => {
      val info = result.group("info") match {
        case Constant.QID => SenderCondition.QID
        case Constant.NICKNAME => SenderCondition.NICKNAME
        case Constant.SEX => SenderCondition.SEX
        case Constant.AGE => SenderCondition.AGE
      }
      result.group("value") match {
        case value if value.startsWith("变量") =>
          SenderCondition(info, value.stripPrefix("变量"), isValueAParam = true)
        case value =>
          SenderCondition(info, value)
      }
    })
  }

  /**
   * 解析会话信息条件
   *
   * @param string  待解析字符串
   * @param context 解析引擎上下文
   * @return 解析结果
   */
  private def parseSessionCondition(string: String)(implicit context: ParseEngineContext): Option[SessionCondition] = {
    import SessionCondition.Constant
    ConditionPattern.sessionCondition.findFirstMatchIn(string).map(result => {
      val info = result.group("info") match {
        case Constant.TYPE => SessionCondition.TYPE
        case k if Constant.QID.contains(k) => SessionCondition.QID
      }
      result.group("value") match {
        case value if value.startsWith("变量") =>
          SessionCondition(info, value.stripPrefix("变量"), isValueAParam = true)
        case value =>
          SessionCondition(info, value)
      }
    })
  }


}
