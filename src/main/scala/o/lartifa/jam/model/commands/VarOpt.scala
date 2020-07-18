package o.lartifa.jam.model.commands

import o.lartifa.jam.common.exception.{ExecutionException, VarNotFoundException}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.VarOpt.Operation

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 变量操作指令
 *
 * Author: sinar
 * 2020/1/4 15:21
 */
case class VarOpt(paramName: String, opt: Operation, value: String = "", isValueAParam: Boolean = false, randomNumber: Option[RandomNumber] = None) extends Command[String] {

  private val operate: (String, String) => String = (opt: @unchecked) match {
    case VarOpt.PLUS => (a, b) => (BigDecimal(a) + BigDecimal(b)).toString()
    case VarOpt.MINUS => (a, b) => (BigDecimal(a) - BigDecimal(b)).toString()
    case VarOpt.TIMES => (a, b) => (BigDecimal(a) * BigDecimal(b)).toString()
    case VarOpt.DIVIDED => (a, b) => (BigDecimal(a) / BigDecimal(b)).toString()
    case VarOpt.MOD => (a, b) => (BigDecimal(a) % BigDecimal(b)).toString()
    case VarOpt.SET => (_, b) => b
  }

  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[String] = async {
    val value: String = randomNumber match {
      case Some(random) =>
        await(random.execute()).toString
      case None =>
        if (isValueAParam)
          await(context.vars.get(this.value)).getOrElse(throw VarNotFoundException(this.value))
        else this.value
    }
    if (opt == VarOpt.SET) {
      await(context.vars.updateOrElseSet(paramName, value))
    } else {
      Try(BigDecimal(value)).getOrElse(throw ExecutionException("试图使用非数字进行加减乘除"))
      val originValue = await(context.vars.get(paramName)).getOrElse(throw VarNotFoundException(paramName))
      Try(BigDecimal(originValue)).getOrElse(throw ExecutionException("变量的原始值不为数字"))
      await(context.vars.update(paramName, operate(originValue, value)))
    }
  }
}

object VarOpt {

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
