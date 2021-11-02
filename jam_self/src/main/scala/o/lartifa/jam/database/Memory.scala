package o.lartifa.jam.database

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import o.lartifa.jam.common.config.{JamConfig, botConfigFile}
import o.lartifa.jam.database.schema.{password, url, user}
import o.lartifa.jam.pool.JamContext
import org.flywaydb.core.Flyway
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
 * Author: sinar
 * 2019/10/12 22:25
 */
object Memory {
  val database: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](schema.name, config = botConfigFile)

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(Memory.getClass)

  /**
   * 记忆初始化
   *
   * @return 初始化结果
   */
  def init(repair: Boolean)(implicit exec: ExecutionContext): Unit = {
    logger.log(s"${AnsiColor.YELLOW}正在构建${JamConfig.config.name}的记忆...")
    val flyway = Flyway.configure().dataSource(url, user, password).load()
    logger.log(s"${AnsiColor.YELLOW}记忆恢复中...")
    if (repair) flyway.repair()
    flyway.migrate()
    logger.log(s"${AnsiColor.GREEN}记忆构建完成")
  }
}
