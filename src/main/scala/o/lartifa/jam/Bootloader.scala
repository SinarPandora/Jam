package o.lartifa.jam

import cc.moecraft.icq.{PicqBotX, PicqConfig}
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.CoolQConfig._
import o.lartifa.jam.common.config.JamConfig._
import o.lartifa.jam.common.config.SystemConfig
import o.lartifa.jam.cool.qq.command.MasterCommands
import o.lartifa.jam.cool.qq.listener.RuleEngineListener
import o.lartifa.jam.engine.JamLoader
import o.lartifa.jam.pool.JamContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * 唤醒果酱
 *
 * Author: sinar
 * 2020/1/2 21:54 
 */
object Bootloader {
  def main(args: Array[String]): Unit = {
    val messageListener = new PicqBotX(new PicqConfig(socketPort).setDebug(SystemConfig.debugMode))
    messageListener.addAccount(name, postUrl, postPort)
    messageListener.getEventManager.registerListeners(RuleEngineListener)
    messageListener.enableCommandManager("！", "!")
    messageListener.getCommandManager.registerCommands(MasterCommands.commands: _*)
    JamContext.logger.set(messageListener.getLoggerInstanceManager.getLoggerInstance(name, SystemConfig.debugMode))
    Await.result(JamLoader.init(), Duration.Inf)
    messageListener.startBot()
    JamContext.logger.get().log(s"${AnsiColor.GREEN}${name}已苏醒")
  }
}
