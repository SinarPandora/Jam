package o.lartifa.jam.cool.qq

import o.lartifa.jam.pool.JamContext

import java.util.concurrent.Executors
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

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

    /**
     * 加载屏蔽列表
     *
     * @return 加载结果
     */
    def loadBanList(): Future[Unit] = {
      JamContext.loggerFactory.get().system.log("开始正在加载禁言列表……")
      Future.sequence(Seq(
        JamContext.variablePool.getOrElseUpdate("Private_Ban_List", "")
          .map(_.split(",").map(_.toLong).toIterable)
          .map(user.addAll),
        JamContext.variablePool.getOrElseUpdate("Group_Ban_List", "")
          .map(_.split(",").map(_.toLong).toIterable)
          .map(group.addAll)
      )).flatMap(_ => {
        JamContext.loggerFactory.get().system.log("禁言列表加载完成！")
        Future.unit
      })
    }
  }
}
