package o.lartifa.jam.onebot

import o.lartifa.jam.onebot.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.blocks.schema.*

/**
 * Schema 编解码冒烟。
 *
 * Author: sinar
 * 2026/11/26
 */
class OneBotModelsSpec extends AnyFlatSpec with Matchers {

  "SendPrivateMsgParams" should "round-trip JSON via Schema" in {
    val raw = SendPrivateMsgParams(10001L, "你好").toJsonString
    raw should include("10001")
    raw should include("你好")
    val back = raw.fromJson[SendPrivateMsgParams]
    back.isRight.`shouldBe`(true)
    back.toOption.get.user_id.`shouldBe`(10001L)
  }

  "SendGroupMsgParams" should "encode group_id" in {
    SendGroupMsgParams(713201079L, "验证券").toJsonString should include("713201079")
  }

  "OneBotApiResponse" should "decode nested message_id" in {
    val raw = """{"status":"ok","retcode":0,"data":{"message_id":99}}"""
    val back = raw.fromJson[OneBotApiResponse]
    back.isRight.`shouldBe`(true)
    back.toOption.get.data.map(_.message_id).`shouldBe`(Some(99L))
  }
}
