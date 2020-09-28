package o.lartifa.jam.model.commands

import ammonite.ops.PipeableImplicit
import cc.moecraft.icq.event.events.message.{EventGroupOrDiscussMessage, EventPrivateMessage}
import o.lartifa.jam.cool.qq.listener.asking.{Answerer, Result}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.commands.Ask.AnswererType

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

/**
 * 询问指令
 *
 * Author: sinar
 * 2020/9/27 10:26
 */
case class Ask(question: RenderStrTemplate, answererType: AnswererType, askMatchers: Map[String, Command[_]], defaultCallback: Option[Command[_]])
  extends Command[Unit] {
  /**
   * 执行
   *
   * @param context 执行上下文
   * @param exec    异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit context: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = async {
    import context.eventMessage._
    await(question.execute()) |> respond
    answerer.?(askMatchers.keySet) { ctx =>
      askMatchers.get(ctx.event.message).orElse(defaultCallback) match {
        case Some(command) =>
          command.execute()
          Future.successful(Result.Complete)
        case None =>
          Future.successful(Result.KeepCountAndContinueAsking)
      }
    }
  }

  /**
   * 获取回答者
   *
   * @param context 执行上下文
   * @return 回答者
   */
  private def answerer(implicit context: CommandExecuteContext): Answerer = {
    answererType match {
      case Ask.CurrentSender =>
        Answerer.sender
      case Ask.AnyGroupFriends =>
        context.eventMessage match {
          case _: EventGroupOrDiscussMessage =>
            Answerer.anyInThisSession
          case _: EventPrivateMessage =>
            Answerer.sender
        }
    }
  }
}

object Ask {

  sealed abstract class AnswererType(val name: String)

  case object CurrentSender extends AnswererType("发送者")

  case object AnyGroupFriends extends AnswererType("任意群友")

}
