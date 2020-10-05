package o.lartifa.jam.cool.qq

import cc.moecraft.icq.{PicqBotX, PicqConfig}
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl, socketPort}
import o.lartifa.jam.common.config.JamConfig.name
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.common.util.LoggerFactory
import o.lartifa.jam.cool.qq.command.MasterCommands
import o.lartifa.jam.cool.qq.listener.{EventMessageListener, SystemEventListener}
import o.lartifa.jam.pool.JamContext

/**
 * Bot 客户端生成器
 *
 * Author: sinar
 * 2020/1/13 22:03
 */
object CoolQQLoader {

  /**
   * 创建酷Q客户端
   * @return 尚未启动的酷Q客户端
   */
  def createCoolQQClient(): PicqBotX = {
    val client = new PicqBotX(new PicqConfig(socketPort).setDebug(SystemConfig.debugMode)) {
      /**
       * 启动监听服务
       * 由于 CQHttp 不再维护，使用 mrial 后端时版本号与 CQ 不同，因此此处取消版本检查
       * 不使用自带的禁用检查，因为其会输出红色警告日志
       */
      override def startBot(): Unit = {
        this.getLogger.log(s"${AnsiColor.GREEN}正在启动...")
        this.getHttpServer.start()
      }
    }
    JamContext.loggerFactory.set(new LoggerFactory(client.getLoggerInstanceManager))
    client.addAccount(name, postUrl, postPort)
    client.getEventManager.registerListeners(EventMessageListener, SystemEventListener)
    client.enableCommandManager("！", "!")
    client.getCommandManager.registerCommands(MasterCommands.commands: _*)
    client
  }
}
