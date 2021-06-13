package o.lartifa.jam.cool.qq.listener.base

/**
 * 退出码
 *
 * Author: sinar
 * 2021/6/13 11:11
 */
object ExitCodes extends Enumeration {
  type ExitCode = Value
  val Finish: ExitCode = Value("正常结束")
  val Break: ExitCode = Value("被打断")
  val DueToProb: ExitCode = Value("由于概率")
  val AsUnMatched: ExitCode = Value("视为未捕获")
}
