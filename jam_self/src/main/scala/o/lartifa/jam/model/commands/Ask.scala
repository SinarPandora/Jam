package o.lartifa.jam.model.commands

import cc.moecraft.icq.event.events.message.EventMessage
import o.lartifa.jam.cool.qq.listener.interactive.{Interactive, InteractiveSession}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Ask.{AnswererType, AnyBody}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 询问指令
 *
 * Author: sinar
 * 2020/9/27 10:26
 */
case class Ask(question: RenderStrTemplate, answererType: AnswererType, askMatchers: Map[String, Command[_]], defaultCallback: Option[Command[_]])
  extends Command[Unit] with Interactive {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    val result = await(question.execute())
    reply(result)
    interact(context.msgSender) { (session, event) =>
      if (answererType == AnyBody || event.getSenderId == context.eventMessage.getSenderId) {
        answer(session, event)
      }
    }
  }

  /**
   * 提问与问答
   *
   * @param session 交互式会话
   * @param event   消息对象
   * @param context 执行上下文
   * @param exec    异步上下文
   */
  private def answer(session: InteractiveSession, event: EventMessage)(implicit context: CommandExecuteContext, exec: ExecutionContext): Unit = {
    askMatchers.find {
      case (key, _) => key.equalsIgnoreCase(event.message.trim)
    } match {
      case Some((_, command)) => command.execute().foreach(_ => session.release())
      case None => defaultCallback.map(_.execute().foreach(_ => session.release())) match {
        case None => session.release()
        case _ =>
      }
    }
  }
}

object Ask {

  sealed abstract class AnswererType(val name: String)

  case object CurrentSender extends AnswererType("发送者")

  case object AnyBody extends AnswererType("任何人")

}
