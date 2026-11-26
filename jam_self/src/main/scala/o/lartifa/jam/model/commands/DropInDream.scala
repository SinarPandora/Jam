package o.lartifa.jam.model.commands

import cc.moecraft.logger.{HyLogger, LogLevel}
import o.lartifa.jam.common.protocol.CommonProtocol.{Data, Fail}
import o.lartifa.jam.common.util.{ExtraActor, MasterUtil}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.ActorCreator
import o.lartifa.jam.model.commands.DropInDream.logger
import o.lartifa.jam.plugins.caiyunai.dream.DreamClient.Dream
import o.lartifa.jam.plugins.caiyunai.dream.DreamingActorProtocol.Reply
import o.lartifa.jam.plugins.caiyunai.dream.KeepAliveDreamingActor
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 坠梦指令
 *
 * Author: sinar
 * 2021/10/31 00:32
 */
case class DropInDream(template: RenderStrTemplate) extends Command[Unit] {
  /**
   * 执行
   *
   * @param ctx  执行上下文
   * @param exec 异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = template.execute().map { content =>
    ActorCreator.actorOf(ExtraActor(
      ctx => {
        KeepAliveDreamingActor.instance ! Reply(ctx.self, content)
      },
      _ => {
        case Data(obj: List[?]) =>
          obj.head match {
            case dream: Dream =>
              val content = dream.content.trim.replaceFirst("[,，。！.!？?;；\n\t]+", "")
              content.sliding(200, 200).foreach(reply)
            case other =>
              MasterUtil.notifyAndLog("彩云小梦返回格式不正确，这是个 bug，请上报。返回类型：{}" + other.getClass, LogLevel.ERROR)
              reply("😴")
          }
        case Fail(errMsg) =>
          logger.log("彩云小梦联想失败，错误信息：{}", errMsg)
          reply("😴")
      }
    ))
  }
}

object DropInDream {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(classOf[DropInDream])
}
