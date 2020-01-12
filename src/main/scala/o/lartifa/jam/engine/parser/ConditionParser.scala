package o.lartifa.jam.engine.parser

import o.lartifa.jam.model.conditions.{Condition, ParamCondition, SenderCondition, SessionCondition}

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
   * @param string 待解析字符串
   * @return 解析结果
   */
  def parseCondition(string: String): Option[Condition] = {
    LazyList(parseParamCondition _, parseSenderCondition _, parseSessionCondition _)
      .map(_.apply(string))
      .find(_.isDefined)
      .flatten
  }

  /**
   * 解析变量条件
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseParamCondition(string: String): Option[ParamCondition] = {
    import ParamCondition.Constant
    ConditionPattern.paramCondition.findFirstMatchIn(string).map(result => {
      val paramName = result.group("name")
      val op = result.group("op") match {
        case Constant.gtOp => ParamCondition.gtOp
        case Constant.geOp => ParamCondition.geOp
        case Constant.ltOp => ParamCondition.ltOp
        case Constant.leOp => ParamCondition.leOp
        case Constant.eqOp => ParamCondition.eqOp
        case Constant.neOp => ParamCondition.neOp
      }
      result.group("value") match {
        case value if value.startsWith("变量") =>
          ParamCondition(paramName, op, value.stripPrefix("变量"), isValueAParam = true)
        case value =>
          ParamCondition(paramName, op, value)
      }
    })
  }

  /**
   * 解析发送者信息条件
   *
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseSenderCondition(string: String): Option[SenderCondition] = {
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
   * @param string 待解析字符串
   * @return 解析结果
   */
  private def parseSessionCondition(string: String): Option[SessionCondition] = {
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
