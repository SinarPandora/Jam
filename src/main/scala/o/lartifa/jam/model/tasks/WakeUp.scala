package o.lartifa.jam.model.tasks

import cn.hutool.cron.task.Task
import o.lartifa.jam.common.config.{JamConfig, SystemConfig}
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.pool.JamContext

/**
 * 作息时间 - 起床
 *
 * Author: sinar
 * 2020/1/23 14:07 
 */
class WakeUp extends Task {
  override def execute(): Unit = {
    JamContext.clientConfig.get().setEventPaused(false)
    JamContext.clientConfig.get().setHttpPaused(false)
    if (SystemConfig.debugMode) {
      MasterUtil.notifyMaster(s"${JamConfig.name} 已苏醒")
    }
    JamContext.logger.get().log(s"${JamConfig.name} 已苏醒")
  }
}
