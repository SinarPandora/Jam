package o.lartifa.jam.model.behaviors

import o.lartifa.jam.cool.qq.listener.base.Break
import o.lartifa.jam.cool.qq.listener.base.ExitCodes.ExitCode
import o.lartifa.jam.model.CommandExecuteContext

/**
 * 可打断的执行流程
 *
 * Author: sinar
 * 2021/6/13 12:57
 */
trait Breakable {
  /**
   * 打断当前流程
   * （使用抛出异常的方式）
   *
   * @param exitCode 退出码
   */
  @throws[Break]
  def break[T](exitCode: ExitCode)(implicit context: CommandExecuteContext): T = {
    context.exitCode = exitCode
    throw Break(exitCode)
  }
}
