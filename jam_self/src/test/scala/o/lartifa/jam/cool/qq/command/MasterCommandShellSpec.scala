package o.lartifa.jam.cool.qq.command

import o.lartifa.jam.onebot.domain.{GroupMessage, PrivateMessage, SenderInfo}
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicReference

/**
 * 指令壳解析与分发边界。
 *
 * Author: sinar
 * 2026/11/28
 */
class MasterCommandShellSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  private val lastPrivate = new AtomicReference[Option[String]](None)
  private val lastGroup = new AtomicReference[Option[String]](None)

  private val outbound: OutboundGateway = new OutboundGateway {
    def sendPrivate(userId: Long, message: String, autoEscape: Boolean = false): Long = {
      lastPrivate.set(Some(message))
      1L
    }
    def sendGroup(groupId: Long, message: String, autoEscape: Boolean = false): Long = {
      lastGroup.set(Some(message))
      2L
    }
    def sendDiscuss(discussId: Long, message: String, autoEscape: Boolean = false): Long = -1L
    def setFriendAddRequest(flag: String, approve: Boolean, remark: String = ""): Unit = ()
    def setGroupAddRequest(
      flag: String,
      requestType: String,
      approve: Boolean,
      reason: String = "",
    ): Unit = ()
  }

  override def beforeEach(): Unit = {
    MasterCommandShell.clear()
    lastPrivate.set(None)
    lastGroup.set(None)
  }

  override def afterEach(): Unit = MasterCommandShell.clear()

  "MasterCommandShell.parse" should "require prefix for all chat types" in {
    MasterCommandShell.parse("ping", Seq("!", "！")) shouldBe None
    MasterCommandShell.parse("!ping", Seq("!", "！")).get shouldBe MasterCommandShell.ParsedCommand("ping", Nil)
    MasterCommandShell.parse("！ping hi", Seq("!", "！")).get shouldBe MasterCommandShell.ParsedCommand("ping", List("hi"))
    MasterCommandShell.parse("  !Echo  a  b", Seq("!")).get.name shouldBe "echo"
  }

  it should "reject missing or bare prefix" in {
    MasterCommandShell.parse("!", Seq("!")) shouldBe None
    MasterCommandShell.parse("", Seq("!")) shouldBe None
  }

  "MasterCommandShell.tryHandle" should "continue message path when not a command" in {
    MasterCommandShell.register(Nil)
    MasterCommandShell.tryHandle(groupMsg("!unknown")) shouldBe true
    MasterCommandShell.tryHandle(groupMsg("plain text")) shouldBe true
    MasterCommandShell.tryHandle(privateMsg("ping")) shouldBe true
    lastGroup.get() shouldBe None
    lastPrivate.get() shouldBe None
  }

  private def privateMsg(text: String): PrivateMessage = PrivateMessage(
    postType = "message",
    messageType = "private",
    messageId = 1L,
    senderId = 10001L,
    selfId = 9L,
    time = 1L,
    message = text,
    rawMessage = text,
    font = 0L,
    subType = Some("friend"),
    sender = SenderInfo(10001L),
    outbound = outbound,
  )

  private def groupMsg(text: String): GroupMessage = GroupMessage(
    postType = "message",
    messageType = "group",
    messageId = 2L,
    senderId = 10001L,
    selfId = 9L,
    time = 1L,
    message = text,
    rawMessage = text,
    font = 0L,
    subType = None,
    groupId = 123L,
    sender = SenderInfo(10001L),
    outbound = outbound,
  )
}
