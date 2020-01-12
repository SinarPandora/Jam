package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.{ExecuteException, ParamNotFoundException}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.ParamOpt.Operation

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 变量操作指令
 *
 * Author: sinar
 * 2020/1/4 15:21 
 */
case class ParamOpt(paramName: String, opt: Operation, value: String = "", isValueAParam: Boolean = false, randomNumber: Option[RandomNumber] = None) extends Command[String] {

  private val operate: (String, String) => String = (opt: @unchecked) match {
    case ParamOpt.PLUS => (a, b) => (BigDecimal(a) + BigDecimal(b)).toString()
    case ParamOpt.MINUS => (a, b) => (BigDecimal(a) - BigDecimal(b)).toString()
    case ParamOpt.TIMES => (a, b) => (BigDecimal(a) * BigDecimal(b)).toString()
    case ParamOpt.DIVIDED => (a, b) => (BigDecimal(a) / BigDecimal(b)).toString()
    case ParamOpt.MOD => (a, b) => (BigDecimal(a) % BigDecimal(b)).toString()
    case ParamOpt.SET => (_, b) => b
  }

  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = async {
    val pool = context.variablePool
    val value: String = randomNumber match {
      case Some(random) =>
        await(random.execute()).toString
      case None =>
        if (isValueAParam)
          await(pool.get(this.value)).getOrElse(throw ParamNotFoundException(this.value))
        else this.value
    }
    if (opt == ParamOpt.SET) {
      await(pool.updateOrElseDefault(paramName, value))
    } else {
      Try(BigDecimal(value)).getOrElse(throw ExecuteException("试图使用非数字进行加减乘除"))
      val originValue = await(pool.get(paramName)).getOrElse(throw ParamNotFoundException(paramName))
      Try(BigDecimal(originValue)).getOrElse(throw ExecuteException("变量的原始值不为数字"))
      await(pool.update(paramName, operate(originValue, value)))
    }
  }
}

object ParamOpt {

  sealed class Operation

  case object PLUS extends Operation

  case object MINUS extends Operation

  case object TIMES extends Operation

  case object DIVIDED extends Operation

  case object MOD extends Operation

  case object SET extends Operation

  object Constant {
    val PLUS: String = "增加"
    val MINUS: String = "减少"
    val TIMES: String = "乘以"
    val DIVIDED: String = "除以"
    val MOD: String = "取余"
    val SET: String = "设置为"
  }

}
