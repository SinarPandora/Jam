package o.lartifa.jam.cool.qq.listener.fsm

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}

import scala.concurrent.{ExecutionContext, Future}

object FSMModeRouter {

  /**
   * 监听消息
   *
   * @param event   消息对象
   * @param context 指令执行上下文
   * @param exec    异步执行上下文
   */
  def forward(event: EventMessage)(implicit context: CommandExecuteContext,
                                   exec: ExecutionContext): Future[Boolean] = {
    val mode = modes.get(context.chatInfo)
    if (mode != null) {
      mode.execute().map {
        case Continue => false
        case UnBecome =>
          modes.remove(context.chatInfo)
          false
        case ContinueThenParseMessage => true
      }
    } else Future.successful(true)
  }
}
