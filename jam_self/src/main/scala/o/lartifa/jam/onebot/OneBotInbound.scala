package o.lartifa.jam.onebot

import cc.moecraft.icq.PicqBotX
import o.lartifa.jam.onebot.EventInboundServer
import zio.Fiber

/**
 * 启动自有入站 Server，把原始 JSON 交给 Picq EventParser。
 *
 * Author: sinar
 * 2026/11/26
 */
object OneBotInbound {
  @volatile private var fiberOpt: Option[Fiber.Runtime[Throwable, Nothing]] = None

  /**
   * 在 coolQQ.port.socket 启动入站（幂等：重复调用会先停旧 fiber）。
   */
  def start(bot: PicqBotX, port: Int): Unit = {
    stop()
    val fiber = EventInboundServer.start(
      port,
      data => bot.getEventManager.getEventParser.call(data),
    )
    fiberOpt = Some(fiber)
  }

  def stop(): Unit = {
    fiberOpt.foreach { fiber =>
      ZioRuntime.run(fiber.interrupt)
    }
    fiberOpt = None
  }
}
