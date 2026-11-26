package o.lartifa.jam

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

/**
 * ScalaTest 脚手架冒烟：证明 jam_self/test 能跑通。
 */
@RunWith(classOf[JUnitRunner])
class ScalaTestScaffoldSpec extends AnyFlatSpec with Matchers {
  "ScalaTest scaffold" should "run under sbt test" in {
    1 + 1 shouldBe 2
  }
}
