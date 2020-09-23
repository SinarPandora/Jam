package o.lartifa.jam.common.util

import cc.moecraft.logger.{HyLogger, LoggerInstanceManager}
import o.lartifa.jam.common.config.SystemConfig

/**
 * Logger 工厂
 *
 * Author: sinar
 * 2020/8/30 11:14
 */
class LoggerFactory(hyFactory: LoggerInstanceManager) {
  val system: HyLogger = this.getLogger("SYSTEM")
  /**
   * 获取一个 logger 实例
   *
   * @param prefix 前缀
   * @return logger 实例
   */
  def getLogger(prefix: String): HyLogger = {
    hyFactory.getLoggerInstance(prefix, SystemConfig.debugMode)
  }

  /**
   * 获取一个 logger 实例
   *
   * @param cls 类定义
   * @return logger 实例
   */
  def getLogger(cls: Class[_]): HyLogger = this.getLogger(cls.getSimpleName.stripSuffix("$"))
}



