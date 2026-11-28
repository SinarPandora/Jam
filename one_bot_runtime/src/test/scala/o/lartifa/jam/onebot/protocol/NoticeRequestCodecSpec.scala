package o.lartifa.jam.onebot.protocol

import o.lartifa.jam.onebot.domain.*
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import o.lartifa.jam.onebot.protocol.event.{EventDeduper, NoticeEventCodec, RequestEventCodec}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * notice / request Schema 解码与去重。
 *
 * Author: sinar
 * 2026/11/28
 */
class NoticeRequestCodecSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  private val noopOutbound: OutboundGateway = new OutboundGateway {
    def sendPrivate(userId: Long, message: String, autoEscape: Boolean = false): Long = -1L
    def sendGroup(groupId: Long, message: String, autoEscape: Boolean = false): Long = -1L
    def sendDiscuss(discussId: Long, message: String, autoEscape: Boolean = false): Long = -1L
    def setFriendAddRequest(flag: String, approve: Boolean, remark: String = ""): Unit = ()
    def setGroupAddRequest(
      flag: String,
      requestType: String,
      approve: Boolean,
      reason: String = "",
    ): Unit = ()
  }

  override def afterEach(): Unit = EventDeduper.clear()

  "NoticeEventCodec" should "decode group poke" in {
    val raw =
      """{"post_type":"notice","notice_type":"notify","sub_type":"poke","group_id":1,"user_id":2,"target_id":3,"self_id":9,"time":10}"""
    val evt = NoticeEventCodec.tryDecode(raw).get
    evt shouldBe a[GroupPokeNotice]
    evt.asInstanceOf[GroupPokeNotice].groupId.`shouldBe`(1L)
  }

  it should "decode friend poke without group_id" in {
    val raw =
      """{"post_type":"notice","notice_type":"notify","sub_type":"poke","user_id":2,"target_id":3,"self_id":9,"time":10}"""
    NoticeEventCodec.tryDecode(raw).get shouldBe a[FriendPokeNotice]
  }

  it should "ignore recall (cold path)" in {
    val raw =
      """{"post_type":"notice","notice_type":"group_recall","group_id":1,"user_id":2,"self_id":9,"time":10,"message_id":1}"""
    NoticeEventCodec.tryDecode(raw).isEmpty.`shouldBe`(true)
  }

  "RequestEventCodec" should "decode friend request" in {
    val raw =
      """{"post_type":"request","request_type":"friend","flag":"f1","user_id":2,"self_id":9,"time":10,"comment":"hi"}"""
    val evt = RequestEventCodec.tryDecode(raw, noopOutbound).get
    evt shouldBe a[FriendAddRequest]
    evt.flag.`shouldBe`("f1")
  }

  it should "decode group invite only" in {
    val invite =
      """{"post_type":"request","request_type":"group","sub_type":"invite","flag":"g1","group_id":7,"user_id":2,"self_id":9,"time":10}"""
    RequestEventCodec.tryDecode(invite, noopOutbound).get shouldBe a[GroupInviteRequest]
    val add =
      """{"post_type":"request","request_type":"group","sub_type":"add","flag":"g2","group_id":7,"user_id":2,"self_id":9,"time":10}"""
    RequestEventCodec.tryDecode(add, noopOutbound).isEmpty.`shouldBe`(true)
  }

  "EventDeduper" should "drop duplicates within 2s when enabled" in {
    EventDeduper.isNew("group_poke", "1", "k", 100L, enabled = true).`shouldBe`(true)
    EventDeduper.isNew("group_poke", "1", "k", 101L, enabled = true).`shouldBe`(false)
    EventDeduper.isNew("group_poke", "1", "k", 110L, enabled = true).`shouldBe`(true)
    EventDeduper.isNew("group_poke", "1", "k", 110L, enabled = false).`shouldBe`(true)
  }
}
