package o.lartifa.jam.common.prototype

import java.util.concurrent.CompletionStage
import scala.concurrent.Future
import scala.jdk.FutureConverters.FutureOps
import scala.language.implicitConversions

/**
 * 异步组件
 *
 * Author: sinar
 * 2021/1/14 22:13
 */
trait AsyncComponent {
  implicit def futureAsJava[T](fu: Future[T]): CompletionStage[T] = fu.asJava
}
