package jam.plugins.meme_maker.engine

import org.scalatest.flatspec.AnyFlatSpec

/**
 * API 请求生成器 测试类
 *
 * Author: sinar
 * 2020/11/18 22:52
 */
class MemeAPIRequestTest extends AnyFlatSpec {
  "MemeAPIRequest" should "build a API request body json" in {
    val body = MemeAPIRequest.apply(1, List("foo", "bar"))
    assert(body == """{"id":1,"fillings":{"sentence0":"foo","sentence1":"bar"}}""")
  }
}
