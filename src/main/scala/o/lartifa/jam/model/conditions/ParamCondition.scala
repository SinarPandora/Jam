package o.lartifa.jam.model.conditions

import o.lartifa.jam.common.exception.{ExecutionException, ParamNotFoundException}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.conditions.ParamCondition.Op

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 获取变量进行判断
 *
 * Author: sinar
 * 2020/1/4 16:17 
 */
case class ParamCondition(paramName: String, op: Op, value: String, isValueAParam: Boolean = false) extends Condition {

  import ParamCondition._

  /**
   * 是否匹配该种情况
   *
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   * @return 匹配结果
   */
  override def isMatched(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Boolean] = async {
    val pool = context.variablePool
    val paramValue: String = await(pool.get(paramName)).getOrElse(throw ParamNotFoundException(paramName))
    val comparedValue: String = if (isValueAParam) await(pool.get(value)).getOrElse(throw ParamNotFoundException(value)) else value
    if (op != eqOp && op != neOp) {
      val a = Try(BigDecimal(paramValue)).getOrElse(throw ExecutionException("执行数值比较的左侧必须为数字"))
      val b = Try(BigDecimal(comparedValue)).getOrElse(throw ExecutionException("执行数值比较的右侧必须为数字"))
      (op: @unchecked) match {
        case ParamCondition.gtOp => a > b
        case ParamCondition.geOp => a >= b
        case ParamCondition.ltOp => a < b
        case ParamCondition.leOp => a <= b
      }
    } else {
      (op: @unchecked) match {
        case ParamCondition.eqOp => paramValue == comparedValue
        case ParamCondition.neOp => paramValue != comparedValue
      }
    }
  }
}

object ParamCondition {

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