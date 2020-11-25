package o.lartifa.jam

import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.backend.MiraiBackend
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl}
import o.lartifa.jam.common.config.JamConfig._
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
    if (args.contains("--help")) help()
    val client = CoolQQLoader.createCoolQQClient()
    JamContext.bot.set(client)
    JamContext.httpApi.getAndSet(() => client.getAccountManager.getNonAccountSpecifiedApi)
    JamContext.clientConfig.getAndSet(client.getConfig)
    client.getHttpServer.start()
    MiraiBackend.startAndConnectToBackEnd(args)(() => {
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}已连接 Mirai 后端，正在刷新数据...")
      client.addAccount(name, postUrl, postPort)
      Await.result(JamLoader.init(client, args), Duration.Inf)
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}数据刷新成功！开始接收消息")
    })
    JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}${name}已恢复生命体征")
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
        |""".stripMargin)
    sys.exit(0)
  }

  /**
   * 运行时配置JVM参数
   *
   * @param args 命令行参数
   */
  def setUpJVMParameters(args: Array[String]): Unit = {
    val configPath = args
      .find(_.startsWith("--config="))
      .map(_.stripPrefix("--config="))
      .getOrElse("../conf/bot.conf")
    System.setProperty("config.file", configPath)
  }
}
