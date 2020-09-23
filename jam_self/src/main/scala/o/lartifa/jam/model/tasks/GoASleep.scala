package o.lartifa.jam.model.tasks

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.{JamCharacter, JamConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.common.util.PicqBotUtil.Helper
import o.lartifa.jam.model.tasks.GoASleep.goASleep
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 作息时间 - 睡眠
 *
 * Author: sinar
 * 2020/1/23 14:10
 */
class GoASleep extends JamCronTask(name = "睡眠") {
  override def run()(implicit exec: ExecutionContext): Future[Unit] = {
    MasterUtil.notifyMaster(JamCharacter.ForMaster.goodNight)
    goASleep()
    Future.successful(())
  }
}

object GoASleep {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(GoASleep.getClass)

  /**
   * 就寝
   */
  def goASleep(): Unit = {
    JamContext.bot.get().switchToSleepMode()
    logger.log(s"${JamConfig.name}已经休眠")
  }
}
