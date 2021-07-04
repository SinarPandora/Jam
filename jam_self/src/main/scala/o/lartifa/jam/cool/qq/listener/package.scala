package o.lartifa.jam.cool.qq

import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventMessage, EventPrivateMessage}
import o.lartifa.jam.model.patterns.ContentMatcher
import o.lartifa.jam.pool.JamContext

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
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

  object Matchers {
    val globalMatchers: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
    val globalGroupMatchers: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
    val globalPrivateMatchers: AtomicReference[List[ContentMatcher]] = new AtomicReference[List[ContentMatcher]]()
    val customMatchers: AtomicReference[Map[String, Map[Long, List[ContentMatcher]]]] = new AtomicReference()
  }

  object BanList {
    val group: mutable.Set[Long] = mutable.Set[Long]()
    val user: mutable.Set[Long] = mutable.Set[Long]()

    /**
     * 加载屏蔽列表
     *
     * @return 加载结果
     */
    def loadBanList(): Future[Unit] = {
      JamContext.loggerFactory.get().system.log("开始正在加载屏蔽列表……")
      Future.sequence {
        List("Private_Ban_List", "Group_Ban_List")
          .map {
            JamContext.variablePool.getOrElseUpdate(_, "")
              .map(_.split(",").filterNot(_ == "").map(_.toLong).toIterable)
          }
      }.flatMap {
        case priBan :: groupBan :: Nil =>
          user.addAll(priBan)
          group.addAll(groupBan)
          JamContext.loggerFactory.get().system.log("屏蔽列表加载完成！")
          Future.unit
        case _ =>
          JamContext.loggerFactory.get().system.warning("屏蔽列表加载异常！屏蔽功能可能无法正常运行")
          Future.unit
      }
    }

    /**
     * 检查消息是否允许处理
     *
     * @param eventMessage 消息对象
     * @return 是否允许
     */
    def isAllowed(eventMessage: EventMessage): Boolean = {
      eventMessage match {
        case msg: EventGroupOrDiscussMessage =>
          !(BanList.group.contains(msg.getGroup.getId) || BanList.user.contains(msg.getSenderId))
        case msg: EventPrivateMessage => !BanList.user.contains(msg.getSenderId)
      }
    }
  }
}
