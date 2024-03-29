package o.lartifa.jam

import better.files.File
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.backend.MiraiBackend
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl}
import o.lartifa.jam.common.config.{DynamicConfigLoader, JamConfig, SystemConfig}
import o.lartifa.jam.cool.qq.CoolQQLoader
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
    setUpJVMParameters(args)
    DynamicConfigLoader.reload()
    if (args.contains("--help")) help()
    File(SystemConfig.tempDir).createDirectoryIfNotExists()
    val client = CoolQQLoader.createCoolQQClient()
    JamContext.bot.set(client)
    JamContext.setAPIClient(() => client.getAccountManager.getNonAccountSpecifiedApi)
    JamContext.clientConfig.getAndSet(client.getConfig)
    client.getHttpServer.start()
    val afterBoot = () => {
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}已连接后端，正在刷新数据...")
      client.addAccount(JamConfig.config.name, postUrl, postPort)
      Await.result(JamLoader.init(client, args), Duration.Inf)
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}数据刷新成功！开始接收消息")
    }
    if (args.contains("--no_backend")) afterBoot()
    else MiraiBackend.startAndConnectToBackEnd(args)(afterBoot)
    JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}${JamConfig.config.name}已恢复生命体征")
  }

  /**
   * 输出帮助信息
   */
  def help(): Unit = {
    println(
      """果酱命令行启动器
        |使用方式:
        |windows: jam.bat
        |*nix: ./jam
        |
        |可选参数：
        |--flyway_repair  执行数据迁移修复
        |--help           输出该提示
        |--config=xxx     手动指定配置文件位置
        |--use_mirai3     使用 mirai 3 后端（实验性）
        |--no_backend     不监控任何 backend（适合于独立运行CQHTTP后端）
        |""".stripMargin)
    sys.exit(0)
  }

  /**
   * 运行时配置JVM参数
   *
   * @param args 命令行参数
   */
  private def setUpJVMParameters(args: Array[String]): Unit = {
    val configPath = args
      .find(_.startsWith("--config="))
      .map(_.stripPrefix("--config="))
      .getOrElse("../conf/bot.conf")
    System.setProperty("config.file", configPath)
  }
}
