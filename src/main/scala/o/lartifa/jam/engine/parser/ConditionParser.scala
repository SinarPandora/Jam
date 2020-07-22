package o.lartifa.jam.engine.parser

import ammonite.ops.PipeableImplicit
import o.lartifa.jam.model.conditions.{Condition, VarCondition}

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
  def parseCondition(string: String)(implicit context: ParseEngineContext): Option[Condition] = parseParamCondition(string)

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


}
