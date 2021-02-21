package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.{Actor, ActorRef, Props}
import cc.moecraft.icq.event.events.message.EventMessage
import glokka.Registry
import o.lartifa.jam.common.util.GlobalConstant.MessageType
import o.lartifa.jam.cool.qq.listener.fsm.{Mode, ModeRtnCode}
import o.lartifa.jam.model.CommandExecuteContext
import o.lartifa.jam.model.behaviors.SendMsgToActorWhenReady
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.plugins.caiyunai.dream.DreamActor.ContentUpdated
import o.lartifa.jam.plugins.caiyunai.dream.StartDreaming.{Link, UnLink}
import o.lartifa.jam.pool.JamContext

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

/**
 * 连接到梦境
 *
 * Author: sinar
 * 2021/2/21 23:08
 */
object LinkToDream extends Command[Unit] with SendMsgToActorWhenReady {
  private val pattern: Regex = """(群聊|私聊)\s*([0-9]+)""".r("type", "qid")

  /**
   * 执行
   *
   * @param ctx 执行上下文
   * @param ec  异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit ctx: CommandExecuteContext, ec: ExecutionContext): Future[Unit] = {
    pattern.findFirstMatchIn(ctx.eventMessage.message) match {
      case None =>
        reply("没有找到会话信息")
      case Some(result) =>
        val chatIdString = s"${if (result.group("type") == "群聊") MessageType.GROUP else MessageType.PRIVATE}_" +
          result.group("qid")
        if (ctx.chatInfo.toString == chatIdString) {
          reply("不能对当前聊天进行梦境连接")
        } else become(new Mode {
          // 初始化
          private val isReady: AtomicBoolean = new AtomicBoolean(false)
          val innerListener: ActorRef = JamContext.actorSystem.actorOf(Props(new Actor {

            override def preStart(): Unit = {
              JamContext.registry ! Registry.Lookup(s"caiyun_worker_$chatIdString")
            }

            /**
             * 初始化模式
             */
            override def receive: Receive = {
              case Registry.Found(_, ref) =>
                ref ! Link(self, ctx.chatInfo)
                context.become(listening(ref))
              case Registry.NotFound(_) =>
                reply("")
                unBecomeToNormal()
            }

            /**
             * 链接模式
             */
            def listening(worker: ActorRef): Receive = {
              case ContentUpdated(_, delta, isAppend, exitEvent) =>
                s"${if (isAppend) "文章被覆盖为：\n" else ""}$delta".sliding(200, 200).foreach(reply)
                if (exitEvent) {
                  reply("梦境会话结束，链接将自动关闭")
                  context.stop(self)
                }
              case eventMessage: EventMessage =>
                if (eventMessage.message.stripPrefix("-").trim == "关闭梦境链接") {
                  worker ! UnLink(self, ctx.chatInfo)
                  reply("梦境连接已关闭")
                  context.stop(self)
                }
            }
          }))

          override def execute()(implicit ctx: CommandExecuteContext, ec: ExecutionContext): Future[ModeRtnCode] = {
            sendMsgToActorWhenReady(isReady, innerListener)
          }
        })
    }
    Future.unit
  }
}
