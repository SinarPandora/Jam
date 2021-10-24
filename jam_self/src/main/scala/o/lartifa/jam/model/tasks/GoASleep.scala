package o.lartifa.jam.model.tasks

import better.files.File
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.{BotConfig, JamConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.common.util.PicqBotUtil.Helper
import o.lartifa.jam.model.tasks.GoASleep.{goASleep, logger}
import o.lartifa.jam.plugins.JamPluginLoader
import o.lartifa.jam.pool.JamContext

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * 作息时间 - 睡眠
 *
 * Author: sinar
 * 2020/1/23 14:10
 */
class GoASleep(name: String) extends JamCronTask(name) {
  override def run()(implicit exec: ExecutionContext): Future[Unit] = {
    MasterUtil.notifyMaster(JamConfig.config.forMaster.goodNight)
    goASleep()
    // 清理消息记录
    val period = SystemConfig.cleanUpMessagePeriod
    if (period != -1) {
      logger.log(s"正在清理超过${period}天的消息记录...")
      JamContext.messagePool.cleanUpMessage(period).map(count => {
        logger.log(s"${count}条消息记录已清理")
      })
    }
    logger.log(s"正在清理缓存文件夹...")
    Try(File(SystemConfig.tempDir).list.foreach(file => {
      if (file.isDirectory) file.clear() else file.delete()
    })).recover(err => {
      logger.warning("缓存文件夹清理失败，某些文件可能被占用，清理任务将在下次睡眠后进行")
      err
    })
    logger.log(s"缓存文件夹清理完成...")
    Future {
      // 乱序执行睡后任务
      JamPluginLoader.loadedComponents.afterSleepTasks.par.foreach(_.apply())
    }
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
    logger.log(s"${BotConfig.name}已经休眠")
  }
}
