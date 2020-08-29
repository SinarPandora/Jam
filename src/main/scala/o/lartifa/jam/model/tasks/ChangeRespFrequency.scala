package o.lartifa.jam.model.tasks

import o.lartifa.jam.common.config.{JamConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
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
    JamContext.ruleEngineListener.get().adjustFrequency(freq)
    if (SystemConfig.debugMode) {
      MasterUtil.notifyMaster(s"${JamConfig.name}的回复频已变更为：$freq%")
    }
    JamContext.logger.get().log(s"${JamConfig.name}的回复频率已变更为：$freq%")
    Future.successful(())
  }
}

object ChangeRespFrequency {
  /**
   * 构造器
   *
   * @param freq 回复频率
   * @return 调整回复频率 task 实例
   */
  def apply(freq: Int): ChangeRespFrequency = new ChangeRespFrequency(freq)
}
