package o.lartifa.jam.plugins.caiyunai.dream

import akka.actor.{Actor, ActorRef, Props, Terminated}
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.logger.HyLogger
import glokka.Registry
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.cool.qq.listener.fsm.{Mode, ModeRtnCode}
import o.lartifa.jam.model.behaviors.SendMsgToActorWhenReady
import o.lartifa.jam.model.commands.Command
import o.lartifa.jam.model.{ChatInfo, CommandExecuteContext}
import o.lartifa.jam.plugins.caiyunai.dream.DreamActor._
import o.lartifa.jam.pool.JamContext

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}

/**
 * 坠梦指令
 *
 * Author: sinar
 * 2021/2/20 20:11
 */
object StartDreaming extends Command[Unit] with SendMsgToActorWhenReady {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(StartDreaming.getClass)

  case class Link(sender: ActorRef, chatInfo: ChatInfo)

  case class UnLink(sender: ActorRef, chatInfo: ChatInfo)

  case class RegisterMySelf(dreamActor: ActorRef)

  /**
   * 执行
   *
   * @param ctx  执行上下文
   * @param exec 异步上下文
   * @return 异步返回执行结果
   */
  override def execute()(implicit ctx: CommandExecuteContext, exec: ExecutionContext): Future[Unit] = Future {
    become(new Mode {
      private val isReady: AtomicBoolean = new AtomicBoolean(false)
      // 初始化
      val innerWorker: ActorRef = JamContext.actorSystem.actorOf(Props(new Actor {

        override def preStart(): Unit = {
          // 试图注册新的 actor
          JamContext.registry ! Registry.Register(s"caiyun_actor_${ctx.chatInfo.serialize}",
            Props(classOf[DreamActor], ctx.eventMessage))
        }

        /**
         * 初始化模式
         */
        override def receive: Receive = {
          case Registry.Found(_, ref) =>
            reply(s"当前聊天存在一个做梦的${JamConfig.name}，已重新连接意识探针")
            logger.log(s"已连接到存在的梦境会话，聊天信息：${ctx.chatInfo.toString}")
            self ! RegisterMySelf(ref)
          case Registry.Created(_, ref) =>
            ref ! Start(self)
          case Ready(sender) =>
            logger.log(s"已创建新的梦境会话，聊天信息：${ctx.chatInfo.toString}")
            self ! RegisterMySelf(sender)
          case FailToStart(sender) =>
            logger.log(s"梦境会话创建失败，聊天信息：${ctx.chatInfo.toString}")
            JamContext.registry ! Terminated(sender)(existenceConfirmed = true, addressTerminated = false)
            unBecomeToNormal()
            context.stop(self)
          case RegisterMySelf(dreamActor) =>
            JamContext.registry ! Registry.Register(s"caiyun_worker_${ctx.chatInfo.serialize}", innerWorker)
            context.become(registerMySelf(dreamActor))
        }

        /**
         * 注册自己模式
         */
        def registerMySelf(dreamActor: ActorRef): Receive = {
          case Registry.Registered(_, _) =>
            context.become(working(dreamActor))
            isReady.getAndSet(true)
            logger.log(s"彩云小梦 worker 完成全部启动任务，准备接收消息，聊天信息：${ctx.chatInfo.toString}")
          case Registry.Conflict(_, other, _) =>
            reply("检测到模式冲突，正在清理全部相关会话，请稍后重新启动梦境")
            context.stop(other)
            context.stop(dreamActor)
            deregisterAll(dreamActor)
            unBecomeToNormal()
            reply("清理完成，现在可以重新启动梦境")
            context.stop(self)
        }

        /**
         * 工作模式
         */
        def working(dreamActor: ActorRef, linker: Map[ChatInfo, ActorRef] = Map.empty): Receive = {
          case eventMessage: EventMessage =>
            dreamActor ! Event(self, eventMessage)
          case event@ContentUpdated(sender, _, _, exitEvent) =>
            linker.values.foreach(ref => ref ! event.copy(sender = self))
            if (exitEvent) {
              logger.log(s"收到结束事件，worker 正在停止运行，聊天信息：${ctx.chatInfo.toString}")
              unBecomeToNormal()
              deregisterAll(sender)
              context.stop(self)
            }
          case Link(sender, chatInfo) =>
            context.become(working(dreamActor, linker + (chatInfo -> sender)))
          case UnLink(_, chatInfo) =>
            context.become(working(dreamActor, linker - chatInfo))
        }

        /**
         * 注销梦境 actor 和 worker
         *
         * @param dreamActor 梦境 Actor 地址
         */
        private def deregisterAll(dreamActor: ActorRef): Unit = {
          JamContext.registry ! Terminated(dreamActor)(existenceConfirmed = true, addressTerminated = false)
          JamContext.registry ! Terminated(self)(existenceConfirmed = true, addressTerminated = false)
        }
      }))

      override def execute()(implicit ctx: CommandExecuteContext, ec: ExecutionContext): Future[ModeRtnCode] = {
        sendMsgToActorWhenReady(isReady, innerWorker)
      }
    })
  }(exec)
}

