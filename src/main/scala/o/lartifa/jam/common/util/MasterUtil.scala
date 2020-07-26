package o.lartifa.jam.common.util

import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.logger.{HyLogger, LogLevel}
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.pool.JamContext

/**
 * 管理者工具
 *
 * Author: sinar
 * 2020/1/23 14:31
 */
object MasterUtil {
  protected lazy val logger: HyLogger = JamContext.logger.get()
  protected lazy val httpApi: IcqHttpApi = JamContext.httpApi.get()()

  /**
   * 通知管理者
   *
   * @param message 信息
   */
  def notifyMaster(message: String): Unit = {
    httpApi.sendPrivateMsg(JamConfig.masterQID, message)
  }

  /**
   * 通知并输出 log
   *
   * @param message  信息
   * @param logLevel log 等级
   * @param error    错误内容（）
   */
  def notifyAndLog(message: String, logLevel: LogLevel, error: Option[Throwable] = None): Unit = {
    httpApi.sendPrivateMsg(JamConfig.masterQID, message)
    if (logLevel == LogLevel.ERROR) {
      error match {
        case Some(e) => logger.error(message, e)
        case None => logger.error(message)
      }
    } else logger.log(logLevel, message)
  }
}
