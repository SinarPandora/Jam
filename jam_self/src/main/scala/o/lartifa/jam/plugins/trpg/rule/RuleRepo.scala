package o.lartifa.jam.plugins.trpg.rule

import cc.moecraft.logger.{HyLogger, LogLevel}
import com.typesafe.config.ConfigFactory
import o.lartifa.jam.common.exception.ParseFailException
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.plugins.trpg.ruleConfigFile
import o.lartifa.jam.pool.JamContext

import scala.util.{Failure, Success, Try}

/**
 * Rule 仓库
 *
 * Author: sinar
 * 2021/7/25 00:22
 */
object RuleRepo {
  private val logger: HyLogger = JamContext.loggerFactory.get().getLogger(this.getClass)
  private var _rules: Map[String, TRPGRule] = Map[String, TRPGRule]()

  /**
   * 全部规则
   *
   * @return 规则
   */
  def rules: Map[String, TRPGRule] = _rules

  /**
   * 重新载入配置
   */
  @throws[ParseFailException]
  def reload(): Unit = {
    if (!ruleConfigFile.exists()) {
      logger.warning("未找到 TRPG 规则文件，部分 TRPG 指令可能无法正常使用")
      return
    }
    val config = ConfigFactory.parseFile(ruleConfigFile)
    Try(TRPGRuleConfReader.readAll(config)) match {
      case Failure(err) =>
        MasterUtil.notifyAndLog(s"TRPG规则解析失败！错误原因：${err.getMessage}", LogLevel.ERROR, error = Some(err))
      case Success(conf) => this._rules = conf.flatMap {
        case (name, conf) => Try(new RuleConfParser(name).parse(conf)) match {
          case Failure(err) =>
            MasterUtil.notifyAndLog(s"TRPG规则${conf.name}解析失败！错误原因：${err.getMessage}，其他规则可正常使用",
              LogLevel.ERROR, error = Some(err))
            None
          case Success(value) =>
            Some(value)
        }
      }.map(it => it.name -> it).toMap
    }
  }
}
