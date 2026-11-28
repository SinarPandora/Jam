package o.lartifa.jam.onebot

import o.lartifa.jam.ob.PicqBotX
import o.lartifa.jam.onebot.domain.ChatMessage
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import o.lartifa.jam.onebot.protocol.event.MessageEventCodec
import o.lartifa.jam.cool.qq.listener.QMessageListener
import o.lartifa.jam.pool.JamContext
import zio.Fiber

/**
 * 启动自有入站 Server：消息走 Schema 领域管道，其它事件仍交 EventParser。
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
  def start(bot: PicqBotX, port: Int, outbound: OutboundGateway): Unit = {
    stop()
    JamContext.outbound.set(outbound)
    val fiber = EventInboundServer.start(
      port,
      data =>
        MessageEventCodec.tryDecode(data, outbound) match {
          case Some(chat) =>
            if (LegacyCommandBridge.tryHandle(chat, bot)) {
              messageHandler(chat)
            }
          case None =>
            bot.getEventManager.getEventParser.call(data)
        },
    )
    fiberOpt = Some(fiber)
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
