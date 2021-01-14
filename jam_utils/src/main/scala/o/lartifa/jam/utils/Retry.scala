package o.lartifa.jam.utils

import scala.annotation.tailrec

/**
 * 重试
 *
 * Author: sinar
 * 2021/1/15 00:38
 */
object Retry {

  class ReachMaxRetriesException(val message: String, val cause: Throwable) extends Exception(message, cause)

  object ReachMaxRetriesException {
    def apply(times: Int, cause: Throwable): ReachMaxRetriesException = {
      new ReachMaxRetriesException(s"达到最大重试次数：${times}次", cause)
    }
  }

  /**
   * 可重试的代码块
   * *如果重试次数为 n，方法最多执行 n + 1 次
   *
   * @param times 最大重试次数
   * @param f     代码块（参数为当前重试次数）
   * @return 成功：Right[T]
   *         失败：Left(ReachMaxRetriesException)
   */
  def retryable[T](times: Int)(f: Int => T): Either[ReachMaxRetriesException, T] = {
    retryable(times, current = 0)(f)
  }

  /**
   * 递归子方法，用于实现重试
   *
   * @param total   最大重试次数
   * @param current 当前重试次数
   * @param f       代码块
   * @return 成功：Right[T]
   *         失败：重试，直到达到最大重试次数，返回 Left(ReachMaxRetriesException)
   */
  @tailrec
  private def retryable[T](total: Int, current: Int)(f: Int => T): Either[ReachMaxRetriesException, T] = {
    try {
      Right(f(current))
    } catch {
      case e: Throwable =>
        if (current == total) {
          Left(ReachMaxRetriesException(total, e))
        } else {
          retryable(total, current + 1)(f)
        }
    }
  }
}
