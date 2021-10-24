package o.lartifa.jam.model.behaviors

import akka.actor.ActorRef
import o.lartifa.jam.cool.qq.listener.fsm.{Continue, Mode, ModeRtnCode, ModeSwitcher}
import o.lartifa.jam.model.CommandExecuteContext

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}

/**
 * 行为：当准备好后，将当前聊天行为变为向指定 actor 发送消息事件
 *
 * Author: sinar
 * 2021/2/21 23:18
 */
trait SendMsgToActorWhenReady extends ModeSwitcher {
  /**
   * 当 actor 准备好后，将当前聊天行为变为向指定 actor 发送消息事件
   * 该方法应该在 become 中使用
   *
   * @param isReady actor 是否准备完毕
   * @param actor   目标 actor
   * @param context 指令执行上下文
   * @return 继续执行当前模式
   */
  def sendMsgToActorWhenReady(isReady: AtomicBoolean, actor: ActorRef)(implicit context: CommandExecuteContext): Future[ModeRtnCode] = {
    if (isReady.get()) {
      become(new Mode {
        override def execute()(implicit ctx: CommandExecuteContext, ec: ExecutionContext): Future[ModeRtnCode] = {
          actor ! ctx.eventMessage
          Future.successful(Continue)
        }
      })
      actor ! context.eventMessage
    }
    Future.successful(Continue)
  }
}
