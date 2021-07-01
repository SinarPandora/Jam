package o.lartifa.jam.cool.qq

import java.util.concurrent.Executors
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
 * Listener 包对象
 *
 * Author: sinar
 * 2020/9/18 21:26
 */
package object listener {
  private[listener] implicit val listenerCommonPool: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  object BanList {
    val group: mutable.Set[Long] = mutable.Set[Long]()
    val user: mutable.Set[Long] = mutable.Set[Long]()
  }
}
