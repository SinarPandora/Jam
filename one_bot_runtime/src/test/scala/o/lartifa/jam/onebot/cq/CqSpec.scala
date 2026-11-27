package o.lartifa.jam.onebot.cq

import o.lartifa.jam.onebot.cq.Cq.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * CQ 组件串对齐 legacy。
 *
 * Author: sinar
 * 2026/11/27
 */
class CqSpec extends AnyFlatSpec with Matchers {

  "Cq.At" should "match legacy ComponentAt" in {
    At(123L).toCq.`shouldBe`("[CQ:at,qq=123]")
  }

  "Cq.ImageBase64" should "prefix base64://" in {
    ImageBase64("abc").toCq.`shouldBe`("[CQ:image,file=base64://abc]")
  }

  "Cq.Music" should "encode source wire name" in {
    Music(9, MusicSource.Netease).toCq.`shouldBe`("[CQ:music,type=163,id=9]")
  }

  "MessageBuilder" should "concatenate components and text" in {
    new MessageBuilder().add(At(1)).add(" hi ").add(Face(2)).toString.`shouldBe`(
      "[CQ:at,qq=1] hi [CQ:face,id=2]",
    )
  }

  "Cq.decodeMessage" should "unescape CQ entities" in {
    Cq.decodeMessage("a&#91;b&#93;&amp;&#44;").`shouldBe`("a[b]&,")
  }
}
