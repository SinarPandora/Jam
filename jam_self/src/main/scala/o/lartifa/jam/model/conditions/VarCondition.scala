package o.lartifa.jam.model.conditions

import o.lartifa.jam.common.exception.{ExecutionException, VarNotFoundException}
import o.lartifa.jam.model.commands.RenderStrTemplate
import o.lartifa.jam.model.conditions.VarCondition.Op
import o.lartifa.jam.model.{CommandExecuteContext, VarKey}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 获取变量进行判断
 *
 * Author: sinar
 * 2020/1/4 16:17
 */
case class VarCondition(varKey: VarKey, op: Op, template: RenderStrTemplate) extends Condition {

  import VarCondition._

  /**
   * 是否匹配该种情况
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 匹配结果
   */
  override def isMatched(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = async {
    val paramValue: String = await(varKey.query).getOrElse(throw VarNotFoundException(varKey))
    val comparedValue: String = await(template.execute())
    if (op != eqOp && op != neOp) {
      val a = Try(BigDecimal(paramValue)).getOrElse(throw ExecutionException("执行数值比较的左侧必须为数字"))
      val b = Try(BigDecimal(comparedValue)).getOrElse(throw ExecutionException("执行数值比较的右侧必须为数字"))
      (op: @unchecked) match {
        case VarCondition.gtOp => a > b
        case VarCondition.geOp => a >= b
        case VarCondition.ltOp => a < b
        case VarCondition.leOp => a <= b
      }
    } else {
      (op: @unchecked) match {
        case VarCondition.eqOp => paramValue == comparedValue
        case VarCondition.neOp => paramValue != comparedValue
      }
    }
  }
}

object VarCondition {

  sealed class Op

  case object gtOp extends Op

  case object geOp extends Op

  case object ltOp extends Op

  case object leOp extends Op

  case object eqOp extends Op

  case object neOp extends Op

  object Constant {
    val gtOp: String = "大于"
    val geOp: String = "不小于"
    val ltOp: String = "小于"
    val leOp: String = "不大于"
    val eqOp: String = "等于"
    val neOp: String = "不等于"
  }

}
