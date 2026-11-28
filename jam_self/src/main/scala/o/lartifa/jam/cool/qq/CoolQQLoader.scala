package o.lartifa.jam.cool.qq

import o.lartifa.jam.ob.{PicqBotX, PicqConfig}
import o.lartifa.jam.common.config.CoolQConfig.socketPort
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.common.util.LoggerFactory
import o.lartifa.jam.cool.qq.command.{MasterCommandShell, MasterCommands}
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
    // 入站由 OneBotInbound（ZIO）负责；指令由 MasterCommandShell，不再 enableCommandManager
    val client = new PicqBotX(new PicqConfig(socketPort).setNoVerify(true).setDebug(SystemConfig.debugMode))
    JamContext.loggerFactory.set(new LoggerFactory(client.getLoggerInstanceManager))
    client
  }

  /**
   * 重载监护人指令
   */
  def reloadMasterCommands(): Unit = {
    MasterCommandShell.register(MasterCommands.commands)
  }
}
