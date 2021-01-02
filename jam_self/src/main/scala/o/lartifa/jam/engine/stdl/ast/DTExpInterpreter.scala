package o.lartifa.jam.engine.stdl.ast

import net.redhogs.cronparser.CronExpressionDescriptor
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.engine.stdl.ast.DefaultInterpretProtocol._

import scala.util.{Failure, Success, Try}

/**
 * 时间表达式解析器
 *
 * Author: sinar
 * 2021/1/1 18:35
 */
object DTExpInterpreter {

  case class InterpreterResult(exp: String, description: String)

  /**
   * 解析并解释时间表达式
   *
   * @param raw 待解析字符串
   * @return 解释结果
   */
  def parseThenInterpret(raw: String): Try[InterpreterResult] = {
    DateTimeExpression.apply(raw) match {
      case Some(exp) => exp match {
        case it@CronExpression(_) => toCronExp(it)
        case it@WeekDay(_, _) => toCronExp(it)
        case it@Time(_, _) => toCronExp(it)
        case it@Date(_, _, _, _) => toCronExp(it)
      }
      case None => Failure(ParseFailException("未找到满足条件的时间表达式"))
    }
  }

  /**
   * 解释为时间表达式
   *
   * @param exp 表达式对象
   * @tparam T 可解释对象
   * @return 解释结果
   */
  def toCronExp[T: Interpret](exp: T): Try[InterpreterResult] = {
    val cronExp = implicitly[Interpret[T]].interpret(exp)
    validationCronExp(cronExp) match {
      case Some(description) => Success(InterpreterResult(cronExp, description))
      case None => Failure(ParseFailException("时间表达式解析失败"))
    }
  }

  /**
   * 校验 Cron 表达式
   *
   * @param cronExp Cron 表达式
   * @return 校验结果
   */
  def validationCronExp(cronExp: String): Option[String] = {
    Try(CronExpressionDescriptor.getDescription(cronExp)).toOption
  }
}
