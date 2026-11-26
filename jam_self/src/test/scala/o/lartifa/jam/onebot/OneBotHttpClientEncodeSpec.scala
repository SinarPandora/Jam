package o.lartifa.jam.onebot

import o.lartifa.jam.onebot.model.SendPrivateMsgParams
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.blocks.schema.*

/**
 * 出站 Schema 编码（不启真实 HTTP）。
 *
 * Author: sinar
 * 2026/11/26
 */
class OneBotHttpClientEncodeSpec extends AnyFlatSpec with Matchers {

  "OneBot Schema params" should "encode fields used by adapter" in {
    val json = SendPrivateMsgParams(42L, "hi").toJsonString
    json should include("42")
    json should include("hi")
  }
}
