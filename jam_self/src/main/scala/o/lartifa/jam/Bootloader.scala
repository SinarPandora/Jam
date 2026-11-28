package o.lartifa.jam

import better.files.File
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.backend.{MiraiBackend, StubOneBotBackend}
import o.lartifa.jam.common.config.CoolQConfig.socketPort
import o.lartifa.jam.common.config.{DynamicConfigLoader, JamConfig, SystemConfig}
import o.lartifa.jam.cool.qq.CoolQQLoader
import o.lartifa.jam.engine.JamLoader
import o.lartifa.jam.onebot.protocol.api.OutboundGateway
import o.lartifa.jam.onebot.OneBotInbound
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
    DynamicConfigLoader.reload()
    File(SystemConfig.tempDir).createDirectoryIfNotExists()

    if (args.contains("--compile")) {
      Await.result(JamLoader.compileOnly(), Duration.Inf)
    }

    val stubBackend = args.contains("--stub_backend")
    if (stubBackend) {
      val stubLog = File(SystemConfig.tempDir) / "stub-onebot.ndjson"
      StubOneBotBackend.start(stubLog.toJava)
    }

    val bot = CoolQQLoader.createJamBot()
    JamContext.bot.set(bot)
    JamContext.setAPIClient(() => bot.api)
    val outbound = OutboundGateway(bot.httpClient)
    // M3 #20 / M4：自有 ZIO HTTP 入站；消息走领域管道
    OneBotInbound.start(bot, socketPort, outbound)
    val afterBoot = () => {
      JamContext.loggerFactory.get().system.log(
        if (stubBackend) s"${AnsiColor.GREEN}已连接 Stub OneBot，正在刷新数据..."
        else s"${AnsiColor.GREEN}已连接后端，正在刷新数据..."
      )
      bot.refreshCache()
      Await.result(JamLoader.init(bot, args), Duration.Inf)
      JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}数据刷新成功！开始接收消息")
    }
    if (stubBackend || args.contains("--no_backend")) afterBoot()
    else MiraiBackend.startAndConnectToBackEnd(args)(afterBoot)
    JamContext.loggerFactory.get().system.log(s"${AnsiColor.GREEN}${JamConfig.config.name}已恢复生命体征")
    // ZIO 入站是后台 fiber；旧 PicqHttpServer 会占住主线程，退役后需显式阻塞
    OneBotInbound.blockUntilStopped()
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
        |--compile        仅编译脚本
        |--flyway_repair  执行数据迁移修复
        |--help           输出该提示
        |--config=xxx     手动指定配置文件位置
        |--use_mirai3     使用 mirai 3 后端（实验性）
        |--no_backend     不监控任何 backend（适合于独立运行CQHTTP后端）
        |--stub_backend   假启动：内置 Stub OneBot API，不依赖真实协议端；向收消息端口 POST 事件即可驱动
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
