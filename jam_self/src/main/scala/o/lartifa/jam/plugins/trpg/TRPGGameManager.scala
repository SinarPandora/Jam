package o.lartifa.jam.plugins.trpg

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.protocol.{Data, Done, Fail}
import o.lartifa.jam.common.util.{ExtraActor, MasterUtil}
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.plugins.trpg.TRPGGameManager.TRPGManage
import o.lartifa.jam.plugins.trpg.TRPGGameManager.TRPGManage.Request
import o.lartifa.jam.plugins.trpg.TRPGInstance.{Exit, Get, Init, Notify}
import o.lartifa.jam.plugins.trpg.data.TRPGGameData
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}


/**
 * TRPG 游戏管理器
 *
 * Author: sinar
 * 2021/8/9 00:45
 */
class TRPGGameManager extends Actor with ActorCreator {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)


  override def receive: Receive = {
    case req: Request => req match {
      case TRPGManage.Register(data, senderRef) => this.register(data, senderRef)
      case TRPGManage.Get(chatInfo, senderRef) => this.get(chatInfo, senderRef)
      case TRPGManage.Release(chatInfo, senderRef) => this.release(chatInfo, senderRef)
      case TRPGManage.Broadcast(msg, senderRef) => this.broadcast(msg, senderRef)
      case TRPGManage.ListAll(senderRef) => this.listAll(senderRef)
    }
    case other => logger.warning(s"TRPG游戏管理器收到未知消息：$other，类型：${other.getClass.getName}，${sender()}")
  }

  /**
   * 创建并注册游戏实例
   *
   * @param data      游戏数据
   * @param senderRef 发送者引用
   */
  def register(data: TRPGGameData, senderRef: ActorRef): Unit = {
    trpgGameRegistry.get(data.chatInfo) match {
      case Some(gameRef) =>
        actorOf(new ExtraActor() {
          override def onStart(): Unit = gameRef ! Get(self)

          override def handle: Receive = {
            case Data(data: TRPGGameData) =>
              senderRef ! Fail(s"请先退出当前游戏：${data.name}")
          }
        })
      case None =>
        val newInstance = context.actorOf(Props(new TRPGInstance(data.chatInfo)))
        actorOf(new ExtraActor() {
          override def onStart(): Unit = newInstance ! Init(data, self)

          override def handle: Receive = {
            case Done =>
              trpgGameRegistry += data.chatInfo -> newInstance
              senderRef ! Data(newInstance)
          }
        })
    }
  }

  /**
   * 获取指定会话中的游戏引用
   *
   * @param chatInfo  会话信息
   * @param senderRef 发送者引用
   */
  def get(chatInfo: ChatInfo, senderRef: ActorRef): Unit = senderRef ! Data(trpgGameRegistry.get(chatInfo))

  /**
   * 释放游戏实例
   *
   * @param chatInfo  会话信息
   * @param senderRef 发送者引用
   */
  def release(chatInfo: ChatInfo, senderRef: ActorRef): Unit = {
    trpgGameRegistry.get(chatInfo) match {
      case Some(gameRef) =>
        actorOf(new ExtraActor() {
          override def onStart(): Unit = gameRef ! Exit(self)

          override def handle: Receive = {
            case Done =>
              trpgGameRegistry.remove(chatInfo)
              senderRef ! Done
            case fail: Fail => senderRef ! fail
          }
        })
      case None => senderRef ! Fail("没有正在运行的游戏")
    }
  }

  /**
   * 全局广播
   *
   * @param msg       消息内容
   * @param senderRef 发送者引用
   */
  def broadcast(msg: String, senderRef: ActorRef): Unit = {
    trpgGameRegistry.foreach { case (chat, ref) =>
      actorOf(new ExtraActor(Some(10.seconds)) {
        override def onStart(): Unit = ref ! Notify(msg, self)

        override def handle: Receive = {
          case Done => logger.debug(s"广播成功，$chat")
          case Timeout(_) =>
            logger.warning(s"该会话广播失败，$chat")
            MasterUtil.notifyMaster(s"%s，该会话广播失败，$chat")
        }
      })
    }
    senderRef ! Done
  }

  /**
   * 列出当前运行中的全部游戏
   *
   * @param senderRef 发送者引用
   */
  def listAll(senderRef: ActorRef): Unit = {
    this.list(ThreadPools.IO)
      .map(data => senderRef ! Data(data))(ThreadPools.DEFAULT)
      .recoverWith(err => {
        logger.error("列出全部游戏实例时出错", err)
        senderRef ! Fail("操作出错，请稍后重试")
        Future.unit
      })(ThreadPools.DEFAULT)
  }

  /**
   * 列出当前运行中的全部游戏
   *
   * @param ec 异步上下文
   * @return 会话信息 -> 游戏名组
   */
  private def list(implicit ec: ExecutionContext): Future[Seq[TRPGGameData]] = Future {
    val latch = new CountDownLatch(trpgGameRegistry.size)
    val dataList: ListBuffer[TRPGGameData] = ListBuffer.empty
    actorOf(new Actor {
      override def preStart(): Unit = {
        trpgGameRegistry.values.foreach(ref => ref ! TRPGInstance.Get(self))
      }

      override def receive: Receive = {
        case Data(data: TRPGGameData) =>
          dataList += data
          latch.countDown()
      }
    })
    latch.await(10, TimeUnit.SECONDS)
    dataList.toList
  }

}

object TRPGGameManager {
  object TRPGManage {
    sealed trait Request
    case class Register(data: TRPGGameData, senderRef: ActorRef) extends Request
    case class Get(chatInfo: ChatInfo, senderRef: ActorRef) extends Request
    case class Release(chatInfo: ChatInfo, senderRef: ActorRef) extends Request
    case class ListAll(senderRef: ActorRef) extends Request
    case class Broadcast(msg: String, senderRef: ActorRef) extends Request
  }
}
