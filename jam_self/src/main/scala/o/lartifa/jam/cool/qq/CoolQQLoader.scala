package o.lartifa.jam.cool.qq

import cc.moecraft.logger.LoggerInstanceManager
import cc.moecraft.logger.environments.ConsoleEnv
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl}
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.common.util.LoggerFactory
import o.lartifa.jam.cool.qq.command.{MasterCommandShell, MasterCommands}
import o.lartifa.jam.onebot.protocol.api.HttpBotApi
import o.lartifa.jam.onebot.{JamBot, OneBotHttpClient}
import o.lartifa.jam.pool.JamContext

/**
 * Bot 客户端生成器
 *
 * Author: sinar
 * 2020/1/13 22:03
 */
object CoolQQLoader {

  /**
   * 带控制台环境的 HyLogger 工厂。
   * 用 ConsoleEnv 而非 ConsoleColoredEnv：后者依赖旧 jansi 原生库，在 Apple Silicon 上会 UnsatisfiedLinkError。
   */
  def createLoggerFactory(): LoggerFactory = {
    val manager = new LoggerInstanceManager()
    manager.addEnvironment(new ConsoleEnv())
    new LoggerFactory(manager)
  }

  /**
   * 创建自有薄壳 JamBot（日志工厂一并就绪）。
   */
  def createJamBot(accessToken: String = ""): JamBot = {
    JamContext.loggerFactory.set(createLoggerFactory())
    val httpClient = new OneBotHttpClient(postUrl, postPort, accessToken)
    val api = new HttpBotApi(httpClient)
    new JamBot(
      name = JamConfig.config.name,
      httpClient = httpClient,
      api = api,
      multiAccountOptimizations = true,
    )
  }

  /**
   * 重载监护人指令
   */
  def reloadMasterCommands(): Unit = {
    MasterCommandShell.register(MasterCommands.commands)
  }
}
