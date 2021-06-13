package o.lartifa.jam.model.commands

import o.lartifa.jam.cool.qq.listener.base.ExitCodes
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.ExecutableCommand.Type

import java.security.SecureRandom
import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2020/1/4 12:37
 */
case class ExecutableCommand(frequency: Type, command: Command[_]) extends Command[Option[_]] {

  val possibility: Int = (frequency: @unchecked) match {
    case ExecutableCommand.ALWAYS => 100
    case ExecutableCommand.RARELY => 25
    case ExecutableCommand.SOMETIME => 50
    case ExecutableCommand.VERY_RARELY => 2
  }

  /**
   * 执行指令
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Option[_]] = {
    if (ExecutableCommand.ALWAYS == frequency || isExecutable) {
      command.execute().map(Some.apply)
    } else {
      break(ExitCodes.DueToProb)
    }
  }

  /**
   * 是否可以执行
   *
   * @return 结果
   */
  def isExecutable: Boolean = new SecureRandom().nextInt(100) < possibility
}

object ExecutableCommand {

  sealed class Type

  case object ALWAYS extends Type

  case object RARELY extends Type

  case object SOMETIME extends Type

  case object VERY_RARELY extends Type

  object Constant {
    val ALWAYS: String = "总是"
    val RARELY: String = "很少"
    val SOMETIME: String = "偶尔"
    val VERY_RARELY: String = "极少"
  }

}
