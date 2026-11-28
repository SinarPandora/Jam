package o.lartifa.jam.onebot.protocol

import o.lartifa.jam.onebot.domain.{GroupMessage, PrivateMessage}
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import o.lartifa.jam.onebot.protocol.event.MessageEventCodec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * 入站消息 Schema 解码。
 *
 * Author: sinar
 * 2026/11/27
 */
class MessageEventCodecSpec extends AnyFlatSpec with Matchers {

  private val noopOutbound: OutboundGateway = new OutboundGateway {
    def sendPrivate(userId: Long, message: String, autoEscape: Boolean = false): Long = -1L
    def sendGroup(groupId: Long, message: String, autoEscape: Boolean = false): Long = -1L
    def sendDiscuss(discussId: Long, message: String, autoEscape: Boolean = false): Long = -1L
  }

  private val groupFixture =
    """{
      |  "post_type": "message",
      |  "message_type": "group",
      |  "sub_type": "normal",
      |  "message_id": 424242,
      |  "group_id": 713201079,
      |  "user_id": 10002,
      |  "self_id": 90001,
      |  "font": 0,
      |  "time": 1700000000,
      |  "message": "验证券",
      |  "raw_message": "验证券",
      |  "sender": {
      |    "user_id": 10002,
      |    "nickname": "验证用户",
      |    "card": "",
      |    "role": "member"
      |  }
      |}""".stripMargin

  "MessageEventCodec" should "decode verify-jam group fixture" in {
    val msg = MessageEventCodec.tryDecode(groupFixture, noopOutbound)
    msg.isDefined.`shouldBe`(true)
    val group = msg.get.asInstanceOf[GroupMessage]
    group.groupId.`shouldBe`(713201079L)
    group.senderId.`shouldBe`(10002L)
    group.message.`shouldBe`("验证券")
    group.sender.nickname.`shouldBe`(Some("验证用户"))
  }

  it should "decode private message and CQ escapes" in {
    val raw =
      """{"post_type":"message","message_type":"private","message_id":1,"user_id":2,"self_id":3,"time":4,"message":"a&#91;b&#93;","raw_message":"a[b]","sub_type":"friend"}"""
    val msg = MessageEventCodec.tryDecode(raw, noopOutbound).get
    msg shouldBe a[PrivateMessage]
    msg.message.`shouldBe`("a[b]")
    msg.rawMessage.`shouldBe`("a[b]")
  }

  it should "ignore non-message post_type" in {
    MessageEventCodec.tryDecode(
      """{"post_type":"notice","notice_type":"poke","message_type":"group","message_id":1,"user_id":2,"self_id":3,"time":4,"message":"x"}""",
      noopOutbound,
    ).isEmpty.`shouldBe`(true)
  }
}
