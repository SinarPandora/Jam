package o.lartifa.jam.database.temporary

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import com.typesafe.config.ConfigFactory
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.database.temporary.schema.{password, url, user}
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
  val database: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](schema.name, config = ConfigFactory.load())

  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(Memory.getClass)

  /**
   * 记忆初始化
   *
   * @return 初始化结果
   */
  def init()(implicit exec: ExecutionContext): Unit = {
    logger.log(s"${AnsiColor.YELLOW}正在构建${JamConfig.name}的记忆...")
    val flyway = Flyway.configure().dataSource(url, user, password).load()
    logger.log(s"${AnsiColor.YELLOW}记忆恢复中...")
    flyway.migrate()
    logger.log(s"${AnsiColor.GREEN}记忆构建完成")
  }
}
