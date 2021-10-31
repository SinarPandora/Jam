package o.lartifa.jam.cool.qq.listener.interactive

import akka.actor.ActorRef
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.exception.ExecutionException
import o.lartifa.jam.common.util.ExtraActor
import o.lartifa.jam.cool.qq.listener.interactive.Interactive.logger
import o.lartifa.jam.cool.qq.listener.interactive.InteractiveSessionProtocol.Manage
import o.lartifa.jam.model.behaviors.ActorCreator
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
    ActorCreator.actorOf(ExtraActor(
      ctx => manager ! Manage.Register(msgSender, f, ctx.self),
      _ => {
        case Manage.Registered(ref) =>
          promise.success(ref)
        case other =>
          logger.warning(s"在创建交互式会话过程中接收到无法处理的消息内容：$other，类型：${other.getClass.getName}，$msgSender")
          promise.failure(ExecutionException(
            s"在创建交互式会话过程中接收到无法处理的消息内容：$other，类型：${other.getClass.getName}，$msgSender"
          ))
      }
    ))
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

object Interactive extends Interactive {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)

  /**
   * 为消息对象创建并开启交互模式
   */
  implicit class InteractiveHelper(event: EventMessage) {
    /**
     * 创建交互
     *
     * @param f 交互函数
     * @return 交互 Session 引用
     */
    def interact(f: InteractiveFunction): Future[ActorRef] = Interactive.interact(SpecificSender(event))(f)
  }
}
