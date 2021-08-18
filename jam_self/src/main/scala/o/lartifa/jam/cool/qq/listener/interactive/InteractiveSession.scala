package o.lartifa.jam.cool.qq.listener.interactive

import akka.actor.{Actor, ActorRef}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.cool.qq.listener.interactive.Interactive.InteractiveFunction
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSession.logger
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

  override def receive: Receive = f(this)

  /**
   * 释放会话
   */
  def release(): Unit = {
    // 结束时注销
    manager ! Manage.Unregister(sender, self)
    context.become(waitingForRelease())
    context.stop(self)
  }

  /**
   * 等待注销
   *
   * @return 逻辑处理偏函数
   */
  def waitingForRelease(): Receive = {
    case Manage.Unregistered(refOpt) =>
      if (refOpt.isEmpty) {
        logger.warning(s"该会话被重复注销：${sender.toString}")
      }
      logger.debug(s"会话已销毁：$sender")
      context.stop(self)
    case other => logger.warning(s"注销会话时收到未知消息：$other，类型：${other.getClass.getName}，$sender")
  }
}

object InteractiveSession {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[InteractiveSession])
}
