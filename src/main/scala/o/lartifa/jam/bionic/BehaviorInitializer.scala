package o.lartifa.jam.bionic

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import cn.hutool.cron.CronUtil
import o.lartifa.jam.pool.{CronTaskPool, JamContext}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 仿生行为初始化器
 *
 * Author: sinar
 * 2020/1/23 14:54
 */
object BehaviorInitializer {

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(BehaviorInitializer.getClass)

  /**
   * 初始化
   */
  def init()(implicit exec: ExecutionContext): Future[Unit] = async {
    logger.log(s"${AnsiColor.YELLOW}正在调整生物钟")
    JamContext.cronTaskPool.getAndSet(CronTaskPool())
    BiochronometerParser.parse()
    CronUtil.start()
    logger.log(s"${AnsiColor.YELLOW}生物钟调整完毕")
  }
}
