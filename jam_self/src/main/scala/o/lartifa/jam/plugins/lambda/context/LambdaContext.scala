package o.lartifa.jam.plugins.lambda.context

import o.lartifa.jam.cool.qq.listener.base.ExitCodes
import o.lartifa.jam.cool.qq.listener.base.ExitCodes.ExitCode
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.Breakable

/**
 * Lambda 上下文
 *
 * Author: sinar
 * 2021/11/11 23:35
 */
class LambdaContext(cmdCtx: CommandExecuteContext) extends Breakable {
  /**
   * 修改退出码
   *
   * @param code 退出码
   */
  def setExitCode(code: ExitCode): Unit = this.cmdCtx.exitCode = code

  /**
   * 打断！
   *
   * @param code 退出码
   */
  def interrupt(code: ExitCode): Unit = break(code)(cmdCtx)

  /**
   * 打断！
   */
  def interrupt(): Unit = break(ExitCodes.Break)(cmdCtx)
}
