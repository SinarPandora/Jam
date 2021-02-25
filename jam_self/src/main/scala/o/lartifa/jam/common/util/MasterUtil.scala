package o.lartifa.jam.common.util

import cc.moecraft.icq.sender.IcqHttpApi
import cc.moecraft.logger.{HyLogger, LogLevel}
import o.lartifa.jam.common.config.{JamCharacter, JamConfig}
import o.lartifa.jam.pool.JamContext

/**
 * 监护人工具
 *
 * Author: sinar
 * 2020/1/23 14:31
 */
object MasterUtil {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(MasterUtil.getClass)
  protected lazy val httpApi: IcqHttpApi = JamContext.httpApi.get()()

  /**
   * 通知监护人
   * 可以在消息里使用 %s 获取对管理者的称呼
   *
   * @param message 信息
   */
  def notifyMaster(message: String): Unit = {
    JamConfig.masterList.foreach(qid =>
      httpApi.sendPrivateMsg(qid, message.format(JamCharacter.ForMaster.name))
    )
  }

  /**
   * 通知并输出 log
   * 可以在消息里使用 %s 获取对管理者的称呼
   *
   * @param message  信息
   * @param logLevel log 等级
   * @param error    错误内容
   */
  def notifyAndLog(message: String, logLevel: LogLevel = LogLevel.LOG, error: Option[Throwable] = None): Unit = {
    JamConfig.masterList.foreach(qid =>
      httpApi.sendPrivateMsg(qid, message.format(JamCharacter.ForMaster.name))
    )
    if (logLevel == LogLevel.ERROR) {
      error match {
        case Some(e) => logger.error(message, e)
        case None => logger.error(message)
      }
    } else logger.log(logLevel, message)
  }

  /**
   * 通知并输出堆栈日志
   *
   * @param message 信息
   * @param e       错误
   */
  def notifyAndLogError(message: String, e: Throwable): Unit = {
    JamConfig.masterList.foreach(qid =>
      httpApi.sendPrivateMsg(qid, message.format(JamCharacter.ForMaster.name))
    )
    logger.error(message, e)
  }
}
