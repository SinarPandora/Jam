package o.lartifa.jam.onebot

import o.lartifa.jam.onebot.domain.{ChatMessage, NoticeEvent, RequestEvent, UnhandledNotice}
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import o.lartifa.jam.onebot.protocol.event.*
import o.lartifa.jam.cool.qq.command.MasterCommandShell
import o.lartifa.jam.cool.qq.listener.{QEventListener, QMessageListener, SystemEventListener}
import o.lartifa.jam.pool.JamContext
import zio.Fiber

/**
 * 启动自有入站 Server：message / notice / request / meta 走领域管道，不再调用 EventParser；
 * 指令由 MasterCommandShell 直接吃 ChatMessage。
 *
 * Author: sinar
 * 2026/11/26
 */
object OneBotInbound {
  @volatile private var fiberOpt: Option[Fiber.Runtime[Throwable, Nothing]] = None

  /** 消息热路径处理器（清醒 / 睡眠可切换）。 */
  @volatile private var messageHandler: ChatMessage => Unit =
    (chat: ChatMessage) => QMessageListener.listenEventMessage(chat)

  def setMessageHandler(handler: ChatMessage => Unit): Unit =
    messageHandler = handler

  def resetMessageHandler(): Unit =
    messageHandler = (chat: ChatMessage) => QMessageListener.listenEventMessage(chat)

  /**
   * 在 coolQQ.port.socket 启动入站（幂等：重复调用会先停旧 fiber）。
   */
  def start(bot: JamBot, port: Int, outbound: OutboundGateway): Unit = {
    stop()
    JamContext.outbound.set(outbound)
    val fiber = EventInboundServer.start(
      port,
      data => dispatch(bot, outbound, data),
    )
    fiberOpt = Some(fiber)
  }

  private def dispatch(bot: JamBot, outbound: OutboundGateway, data: String): Unit = {
    MessageEventCodec.tryDecode(data, outbound) match {
      case Some(chat) =>
        if (MasterCommandShell.tryHandle(chat)) {
          messageHandler(chat)
        }
      case None =>
        NoticeEventCodec.tryDecode(data) match {
          case Some(_: UnhandledNotice) =>
            // 冷路径 notice：本阶段 no-op（SSDL 接通见 #44）
            ()
          case Some(notice) =>
            if (shouldDispatchNotice(bot, notice)) {
              QEventListener.handleNoticeEvent(notice)
            }
          case None =>
            RequestEventCodec.tryDecode(data, outbound) match {
              case Some(request) =>
                SystemEventListener.handleRequestEvent(request)
              case None =>
                MetaEventCodec.tryDecode(data) match {
                  case Some(meta) =>
                    SystemEventListener.handleMetaEvent(meta)
                  case None =>
                    JamContext.loggerFactory.get()
                      .getLogger(OneBotInbound.getClass)
                      .debug(s"忽略未识别入站事件：${data.take(120)}")
                }
            }
        }
    }
  }

  private def shouldDispatchNotice(bot: JamBot, notice: NoticeEvent): Boolean = {
    val enabled = bot.multiAccountOptimizations
    val identifier = notice match {
      case g: o.lartifa.jam.onebot.domain.GroupPokeNotice           => g.groupId.toString
      case g: o.lartifa.jam.onebot.domain.GroupMemberIncreaseNotice => g.groupId.toString
      case g: o.lartifa.jam.onebot.domain.GroupMemberDecreaseNotice => g.groupId.toString
      case f: o.lartifa.jam.onebot.domain.FriendPokeNotice          => f.userId.toString
      case _: UnhandledNotice                                       => return false
    }
    val kind = notice match {
      case _: o.lartifa.jam.onebot.domain.GroupPokeNotice           => "group_poke"
      case _: o.lartifa.jam.onebot.domain.FriendPokeNotice          => "friend_poke"
      case _: o.lartifa.jam.onebot.domain.GroupMemberIncreaseNotice => "group_inc"
      case _: o.lartifa.jam.onebot.domain.GroupMemberDecreaseNotice => "group_dec"
      case _: UnhandledNotice                                       => "unhandled"
    }
    EventDeduper.isNew(kind, identifier, notice.contentKey, notice.time, enabled)
  }

  def stop(): Unit = {
    fiberOpt.foreach { fiber =>
      ZioRuntime.run(fiber.interrupt)
    }
    fiberOpt = None
  }

  /**
   * 阻塞当前线程直到入站 fiber 结束（正常常驻永不返回；失败则抛出）。
   * PicqHttpServer 退役后，Bootloader 需靠此保活 JVM。
   */
  def blockUntilStopped(): Unit = {
    fiberOpt.foreach { fiber =>
      ZioRuntime.run(fiber.join)
    }
  }
}
