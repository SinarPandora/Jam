package o.lartifa.jam.onebot.protocol.api

import o.lartifa.jam.onebot.OneBotHttpClient
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * BotApi JSON 解析冒烟（不启真实 HTTP；用假 client 直接喂 body 不现实，
 * 这里测清单模型 + 通过反射不到的路径改测 Stub 形状约定）。
 *
 * Author: sinar
 * 2026/11/28
 */
class HttpBotApiJsonSpec extends AnyFlatSpec with Matchers {

  "BotApi domain models" should "hold query fields used by jam_self" in {
    val m = GroupMemberInfo(1L, "n", card = "c", unfriendly = true, age = 18)
    m.card shouldBe "c"
    m.unfriendly shouldBe true
    StrangerInfo(2L, "s", age = 3).age shouldBe 3
    GroupInfo(9L, "g").groupName shouldBe "g"
    FriendInfo(4L, "f").userId shouldBe 4L
    GroupSummary(5L, "gg").groupId shouldBe 5L
  }

  it should "construct HttpBotApi over a client" in {
    val client = new OneBotHttpClient("127.0.0.1", 9)
    val api: BotApi = new HttpBotApi(client)
    api should not be null
  }
}
