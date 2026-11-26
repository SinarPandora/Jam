package o.lartifa.jam.pool

import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.{ExecutionContext, Future}

@RunWith(classOf[JUnitRunner])
class DBVarPoolSpec extends AnyFlatSpec with Matchers with ScalaFutures {
  implicit val patience: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(50, Millis))
  implicit val exec: ExecutionContext = ExecutionContext.global

  "DBVarPool.updateOrElseUse" should "update when value exists" in {
    val result = DBVarPool.updateOrElseUse(
      Future.successful(Some("old")),
      Future.successful("new"),
      "default"
    )(exec)
    result.futureValue shouldBe "new"
  }

  it should "return default when missing" in {
    val result = DBVarPool.updateOrElseUse(
      Future.successful(None),
      Future.successful("should-not-run"),
      "default"
    )(exec)
    result.futureValue shouldBe "default"
  }
}
