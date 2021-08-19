package o.lartifa.jam.cool.qq.listener.interactive

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSessionProtocol.Manage
import o.lartifa.jam.model.SpecificSender
import o.lartifa.jam.pool.JamContext

/**
 * 交互式会话管理器
 *
 * Author: sinar
 * 2021/8/16 23:58
 */
object InteractiveSessionManager extends Actor {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)

  override def receive: Receive = receive(Map.empty)

  /**
   * 主要逻辑
   *
   * @param sessions 会话信息
   * @return 逻辑处理偏函数
   */
  def receive(sessions: Map[SpecificSender, ActorRef]): Receive = {
    case req: InteractiveSessionProtocol.Manage.Request =>
      req match {
        case Manage.Register(msgSender, f, senderRef) =>
          // 注册
          val refOpt = sessions.get(msgSender)
          val ref = context.actorOf(Props(new InteractiveSession(f, msgSender, self)))
          context.become(receive(sessions.updated(msgSender, ref)))
          logger.debug(s"已注册交互会话：$msgSender")
          senderRef ! Manage.Registered(ref)
          // 如果存在旧的交互实例，在处理完全部逻辑后发送毒丸停止它
          if (refOpt.isDefined) {
            refOpt.get ! PoisonPill
            logger.debug(s"已尝试停止旧的交互会话：$msgSender")
          }

        case Manage.Unregister(msgSender, senderRef) =>
          // 注销
          val refOpt = sessions.get(msgSender)
          context.become(receive(sessions.removed(msgSender)))
          logger.debug(s"已注销交互会话：$msgSender")
          senderRef ! Manage.Unregistered(refOpt)

        case Manage.Search(msgSender, senderRef) =>
          // 查找
          sessions.get(msgSender) match {
            case Some(ref) => senderRef ! Manage.Found(ref)
            case None => senderRef ! Manage.NotFound
          }
      }
    case other => logger.warning(s"接收到未知消息：$other，类型：${other.getClass.getName}")
  }
}
