package o.lartifa.jam.cool.qq.listener.interactive

import akka.actor.{Actor, ActorRef}
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.cool.qq.listener.QMessageListener
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSession.{Break, logger}
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSessionProtocol.Manage
import o.lartifa.jam.model.SpecificSender
import o.lartifa.jam.pool.JamContext

/**
 * 交互式会话
 *
 * Author: sinar
 * 2021/8/17 00:15
 */
class InteractiveSession(f: InteractiveFunction, sender: SpecificSender, manager: ActorRef) extends Actor {

  override def receive: Receive = {
    case event: EventMessage =>
      try f(this, event) catch {
        case Break => // 只是跳过
      }
    case other => logger.warning(s"收到未知消息：$other，类型：${other.getClass.getName}，$sender")
  }

  /**
   * 释放会话
   */
  def release(): Unit = {
    // 结束时注销
    manager ! Manage.Unregister(sender, self)
    context.become(waitingForRelease())
  }

  /**
   * 跳过后续逻辑
   * * 用于代替 Actor 正常逻辑中无法使用的 return 中断
   */
  def break[T](): T = throw Break

  /**
   * 继续处理消息事件
   * * 将消息事件传递给消息监听器，继续进行 SXDL 捕获
   *
   * @param event 消息事件
   */
  def continue(event: EventMessage): Unit = {
    QMessageListener.processMessage(event)
  }

  /**
   * 等待注销
   *
   * @return 逻辑处理偏函数
   */
  private def waitingForRelease(): Receive = {
    case Manage.Unregistered(refOpt) =>
      if (refOpt.isEmpty) {
        logger.warning(s"该会话被重复注销：${sender.toString}")
      }
      logger.debug(s"会话已销毁：${self.path.name}，$sender")
      context.stop(self)
    case _: EventMessage => // 直接忽略注销过程中收到的其他聊天消息
    case other => logger.warning(s"注销会话时收到未知消息：$other，类型：${other.getClass.getName}，$sender")
  }
}

object InteractiveSession {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[InteractiveSession])
  case object Break extends RuntimeException("不需要处理，只是一个占位符")
}
