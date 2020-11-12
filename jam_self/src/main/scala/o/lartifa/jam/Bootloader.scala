package o.lartifa.jam

import java.io.File

import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.CoolQConfig
import o.lartifa.jam.common.config.CoolQConfig.{postPort, postUrl}
import o.lartifa.jam.common.config.JamConfig._
import o.lartifa.jam.cool.qq.CoolQQLoader
import o.lartifa.jam.engine.JamLoader
import o.lartifa.jam.plugins.rss.SubscriptionPool
import o.lartifa.jam.pool.JamContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._
import scala.util.Try

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
    Await.result(JamLoader.init(args), Duration.Inf)
    client.startBot()
    Try(client.addAccount(name, postUrl, postPort))
    startMiraiBackEnd(() => {
      client.getAccountManager.refreshCache()
    })
    SubscriptionPool.init()
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

  /**
   * 启动 mirai 后端
   * *站在巨人的肩膀上
   *
   * @param afterBooted 启动后执行的回调方法
   */
  def startMiraiBackEnd(afterBooted: () => Unit): Unit = {
    val process = (s"""echo "login $qID $password""""
      #| Process(s"""java -jar ./backend.jar""", new File(CoolQConfig.Backend.Mirai.path)))

    process.lazyLines
      .tapEach(println)
      .filter(line => line.contains(s"[BOT $qID] Login successful"))
      .take(1)
      .foreach(_ => afterBooted())

    sys addShutdownHook {
      println("echo TODO 自动关闭 backend".!!)
    }
  }
}
