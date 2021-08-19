package o.lartifa.jam.cool.qq.listener.interactive

import akka.actor.{Actor, ActorRef, Props}
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.cool.qq.listener.interactive.Interactive.logger
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSessionProtocol.Manage
import o.lartifa.jam.model.{CommandExecuteContext, SpecificSender}
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{Future, Promise}

/**
 * 交互式指令
 *
 * Author: sinar
 * 2021/8/17 01:56
 */
trait Interactive {
  /**
   * 创建交互
   *
   * @param msgSender 特定发送者
   * @param f         交互函数
   * @return 交互 Session 引用
   */
  def interact(msgSender: SpecificSender)(f: InteractiveFunction): Future[ActorRef] = {
    val promise: Promise[ActorRef] = Promise()
    JamContext.actorSystem.actorOf(Props(new Actor {

      override def preStart(): Unit = {
        manager ! Manage.Register(msgSender, f, self)
      }

      override def receive: Receive = {
        case Manage.Registered(ref) =>
          promise.success(ref)
          context.stop(self)
        case other =>
          logger.warning(s"在创建交互式会话过程中接收到无法处理的消息内容：$other，类型：${other.getClass.getName}，$msgSender")
          promise.failure(ExecutionException(
            s"在创建交互式会话过程中接收到无法处理的消息内容：$other，类型：${other.getClass.getName}，$msgSender"
          ))
          context.stop(self)
      }
    }))
    promise.future
  }

  /**
   * 创建交互
   *
   * @param f       交互函数
   * @param context 指令执行上下文
   * @return 交互 Session 引用
   */
  def interact(f: InteractiveFunction)(implicit context: CommandExecuteContext): Future[ActorRef] = {
    this.interact(context.msgSender)(f)
  }
}

object Interactive {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)
}
