package o.lartifa.jam.utils

import org.scalatest.flatspec.AnyFlatSpec

/**
 * 测试 Retry
 *
 * Author: sinar
 * 2021/1/15 00:50
 */
class RetryTest extends AnyFlatSpec {

  import Retry._

  behavior of "对 Retry（重试工具）进行测试"

  it should "当传入函数执行出现错误时，进行最多给定次数的重试" in {
    val result: Either[ReachMaxRetriesException, Int] = retryable(3) { _ =>
      1 / 0
    }
    assert(result.isLeft)
    result match {
      case Left(error) => assert(error.message == "达到最大重试次数：3次")
      case Right(_) => fail()
    }
  }

  it should "当传入函数不出错时，直接返回结果" in {
    val result: Either[ReachMaxRetriesException, Boolean] = retryable(3) { idx =>
      assert(idx == 0)
      true
    }
    assert(result.isRight)
    assert(result.getOrElse(false))
  }

}
