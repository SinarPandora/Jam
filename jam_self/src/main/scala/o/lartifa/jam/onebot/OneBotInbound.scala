package o.lartifa.jam.onebot

import o.lartifa.jam.ob.PicqBotX
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
