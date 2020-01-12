package o.lartifa.jam.common.exception

/**
 * 执行异常
 * Author: sinar
 * 2020/1/4 01:36 
 */
case class ExecuteException(message: String) extends Exception(message) {
  // TODO notify master user
}
