package o.lartifa.jam.plugins.trpg

import akka.actor.{Actor, ActorRef}
import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.protocol.{Data, Done, Fail}
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.model.ChatInfo
import o.lartifa.jam.plugins.trpg.TRPGInstance.*
import o.lartifa.jam.plugins.trpg.data.{TRPGDataRepo, TRPGGameData}
import o.lartifa.jam.pool.{JamContext, ThreadPools}

import scala.concurrent.Future

/**
 * TRPG 游戏实例
 *
 * Author: sinar
 * 2021/8/15 01:58
 */
class TRPGInstance(val chatInfo: ChatInfo) extends Actor {
  override def receive: Receive = {
    case Init(data, senderRef) =>
      context.become(receive(data))
      logger.debug(s"游戏实例已启动，会话：$chatInfo")
      senderRef ! Done
  }

  /**
   * Actor 处理逻辑
   * * 累积更新次数达到三次时自动保存
   *
   * @param data        游戏数据
   * @param updateCount 累积更新次数
   * @return 逻辑处理偏函数
   */
  def receive(data: TRPGGameData, updateCount: Int = 0): Receive = {
    case msg: Request => msg match {
      case Get(sender) => sender ! Data(data)

      case Notify(msg, sender) =>
        chatInfo.chatType match {
          case MessageType.GROUP => api.sendGroupMsg(chatInfo.chatId, msg)
          case MessageType.PRIVATE => api.sendPrivateMsg(chatInfo.chatId, msg)
          case MessageType.DISCUSS => api.sendDiscussMsg(chatInfo.chatId, msg)
        }
        sender ! Done

      case Update(sender, data) =>
        if (updateCount == 4) {
          saveAsync(data)
          context.become(receive(data))
        } else {
          context.become(receive(data, updateCount + 1))
        }
        sender ! Done

      case Exit(sender) =>
        TRPGDataRepo.saveMetadataAndStatus(data)
          .map(_ => {
            sender ! Done
            logger.debug(s"游戏实例已关闭，会话：$chatInfo")
            context.stop(self)
          })(ThreadPools.DEFAULT)
          .recoverWith(err => {
            logger.error(err)
            sender ! Fail("保存游戏失败，请稍后重试，若此问题连续多次出现，请尝试重启 bot")
            Future.unit
          })(ThreadPools.DEFAULT)
    }
  }

  /**
   * 保存游戏数据
   *
   * @param data 游戏数据
   */
  def saveAsync(data: TRPGGameData): Unit = {
    TRPGDataRepo.saveMetadataAndStatus(data)
      .map { _ =>
        logger.debug(s"游戏数据已自动保存，会话${data.chatInfo}")
      }(ThreadPools.DEFAULT)
      .recoverWith(err => {
        logger.error(s"游戏自动保存失败，会话${data.chatInfo}", err)
        data.kpList.foreach(kp => api.sendPrivateMsg(kp,
          s"""游戏自动保存失败
             |如果只是偶尔出现一次，可以忽略这个错误；
             |若该问题连续出现，请尝试联系 Bot 的拥有者
             |会话：${data.chatInfo}""".stripMargin))
        Future.unit
      })(ThreadPools.DEFAULT)
  }


}

object TRPGInstance {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[TRPGInstance])
  private lazy val api: IcqHttpApi = JamContext.httpApi.get().apply()

  sealed trait Request
  case class Get(sender: ActorRef) extends Request
  case class Notify(msg: String, sender: ActorRef) extends Request
  case class Update(sender: ActorRef, data: TRPGGameData) extends Request
  case class Exit(sender: ActorRef) extends Request

  sealed trait LifeCycle
  case class Init(data: TRPGGameData, senderRef: ActorRef) extends LifeCycle
  case object Stop extends LifeCycle
}
