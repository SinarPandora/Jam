package o.lartifa.jam

import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.JamConfig._
import o.lartifa.jam.cool.qq.CoolQQLoader
import o.lartifa.jam.engine.JamLoader
import o.lartifa.jam.plugins.rss.SubscriptionPool
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
    if (args.contains("--help")) help()
    val client = CoolQQLoader.createCoolQQClient()
    Await.result(JamLoader.init(args), Duration.Inf)
    JamContext.clientConfig.getAndSet(client.getConfig)
    JamContext.httpApi.getAndSet(() => client.getAccountManager.getNonAccountSpecifiedApi)
    client.startBot()
    SubscriptionPool.init()
    JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}${name}已苏醒")
  }

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
        |""".stripMargin)
    sys.exit(0)
  }
}
