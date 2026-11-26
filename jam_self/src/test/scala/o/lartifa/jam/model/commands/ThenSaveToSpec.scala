package o.lartifa.jam.model.commands

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThenSaveToSpec extends AnyFlatSpec with Matchers {
  "ThenSaveTo.render" should "map boolean and unit results" in {
    ThenSaveTo.render(true) shouldBe "是"
    ThenSaveTo.render(false) shouldBe "否"
    ThenSaveTo.render(()) shouldBe "没有值"
    ThenSaveTo.render(42) shouldBe "42"
  }
}
