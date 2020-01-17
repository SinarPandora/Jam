package o.lartifa.jam.cool.qq

import cc.moecraft.icq.{PicqBotX, PicqConfig}
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl, socketPort}
import o.lartifa.jam.common.config.JamConfig.name
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.cool.qq.command.MasterCommands
import o.lartifa.jam.cool.qq.listener.{RuleEngineListener, SystemEventListener}

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
    val client = new PicqBotX(new PicqConfig(socketPort).setDebug(SystemConfig.debugMode))
    client.addAccount(name, postUrl, postPort)
    client.getEventManager.registerListeners(RuleEngineListener, SystemEventListener)
    client.enableCommandManager("！", "!")
    client.getCommandManager.registerCommands(MasterCommands.commands: _*)
    client
  }
}
