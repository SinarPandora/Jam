package o.lartifa.jam.common.exception

/**
 * 执行异常
 * Author: sinar
 * 2020/1/4 01:36
 */
class ExecutionException(message: String) extends Exception(message)

object ExecutionException {
  def apply(message: String): ExecutionException = new ExecutionException(message)
}
