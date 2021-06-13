package o.lartifa.jam.cool.qq.listener.base

import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.cool.qq.listener.base.ExitCodes.ExitCode

/**
 * 打断 SXDL 执行
 *
 * Author: sinar
 * 2021/6/13 11:27
 */
class Break(val exitCode: ExitCode)
  extends ExecutionException(s"(该异常一般情况下属于正常的业务逻辑)SXDL 执行被打断，退出码为：$exitCode")

object Break {
  def apply(exitCode: ExitCode): Break = new Break(exitCode)
}
