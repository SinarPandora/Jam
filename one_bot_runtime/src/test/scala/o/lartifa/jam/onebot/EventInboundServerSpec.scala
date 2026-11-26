package o.lartifa.jam.onebot

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.*
import zio.http.*

import java.util.concurrent.atomic.AtomicReference

/**
 * 入站路由：JSON POST → 回调，返回 204。
 *
 * Author: sinar
 * 2026/11/26
 */
class EventInboundServerSpec extends AnyFlatSpec with Matchers {

  "EventInboundServer.routes" should "forward JSON body and respond 204" in {
    val captured = new AtomicReference[String]()
    val app = EventInboundServer.routes(captured.set)
    val req = Request
      .post(URL.root, Body.fromString("""{"post_type":"message"}"""))
      .addHeader(Header.ContentType(MediaType.application.json))

    val resp = ZioRuntime.run(ZIO.scoped(app.runZIO(req)))
    resp.status.`shouldBe`(Status.NoContent)
    captured.get() should include("message")
  }

  it should "reject non-JSON content type" in {
    val app = EventInboundServer.routes(_ => ())
    val req = Request.post(URL.root, Body.fromString("x"))
    val resp = ZioRuntime.run(ZIO.scoped(app.runZIO(req)))
    resp.status.`shouldBe`(Status.Ok)
  }
}
