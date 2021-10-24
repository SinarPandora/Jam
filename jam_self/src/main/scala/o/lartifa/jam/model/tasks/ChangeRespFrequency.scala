package o.lartifa.jam.model.tasks

import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.config.{BotConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.cool.qq.listener.QMessageListener
import o.lartifa.jam.model.tasks.ChangeRespFrequency.logger
import o.lartifa.jam.pool.JamContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * 调整回复频率
 *
 * Author: sinar
 * 2020/1/23 14:26
 */
class ChangeRespFrequency(val freq: Int) extends JamCronTask(name = "回复频率变更") {
  override def run()(implicit exec: ExecutionContext): Future[Unit] = {
    QMessageListener.adjustFrequency(freq)
    if (SystemConfig.debugMode) {
      MasterUtil.notifyMaster(s"${BotConfig.name}的回复频已变更为：$freq%")
    }
    logger.log(s"${BotConfig.name}的回复频率已变更为：$freq%")
    Future.successful(())
  }
}

object ChangeRespFrequency {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(ChangeRespFrequency.getClass)
  /**
   * 构造器
   *
   * @param freq 回复频率
   * @return 调整回复频率 task 实例
   */
  def apply(freq: Int): ChangeRespFrequency = new ChangeRespFrequency(freq)
}
