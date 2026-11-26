package o.lartifa.jam.common.util

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.util.Timeout
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

/**
 * Pekko ExtraActor 协议冒烟：收到一条消息后执行 handler 并停止。
 */
@RunWith(classOf[JUnitRunner])
class ExtraActorProtocolSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private val system = ActorSystem("extra-actor-spec")

  override def afterAll(): Unit = {
    system.terminate()
  }

  "ExtraActor" should "handle one message then stop" in {
    val seen = new AtomicReference[Option[Any]](None)
    val ref = system.actorOf(ExtraActor(
      _ => (),
      _ => {
        case msg => seen.set(Some(msg))
      }
    ))
    ref ! "ping"
    Thread.sleep(200)
    seen.get() shouldBe Some("ping")
  }

  it should "expose pekko Timeout type" in {
    Timeout(1.second).duration shouldBe 1.second
  }
}
