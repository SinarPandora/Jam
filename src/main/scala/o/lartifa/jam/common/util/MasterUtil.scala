package o.lartifa.jam.common.util

import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.pool.JamContext

/**
 * 管理者工具
 *
 * Author: sinar
 * 2020/1/23 14:31 
 */
object MasterUtil {
  /**
   * 通知管理者
   *
   * @param message 信息
   */
  def notifyMaster(message: String): Unit = {
    JamContext.httpApi.get()().sendPrivateMsg(JamConfig.masterQID, message)
  }
}
