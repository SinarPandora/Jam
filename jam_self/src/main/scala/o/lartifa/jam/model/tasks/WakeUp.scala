package o.lartifa.jam.model.tasks

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.{JamCharacter, JamConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.common.util.PicqBotUtil.Helper
import o.lartifa.jam.model.tasks.WakeUp.wakeUp
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 作息时间 - 起床
 *
 * Author: sinar
 * 2020/1/23 14:07
 */
class WakeUp(name: String) extends JamCronTask(name) {
  override def run()(implicit exec: ExecutionContext): Future[Unit] = {
    MasterUtil.notifyMaster(JamCharacter.ForMaster.goodMorning)
    wakeUp()
    Future.successful(())
  }
}

object WakeUp {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(WakeUp.getClass)

  /**
   * 起床
   */
  def wakeUp(): Unit = {
    JamContext.bot.get().switchToWakeUpMode()
    logger.log(s"${JamConfig.name}已苏醒")
  }
}
