package o.lartifa.jam.database.temporary

import cc.moecraft.logger.HyLogger
import cc.moecraft.logger.format.AnsiColor
import com.typesafe.config.ConfigFactory
import o.lartifa.jam.common.config.JamConfig
import o.lartifa.jam.database.temporary.schema.Tables
import o.lartifa.jam.pool.JamContext
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
 * Author: sinar
 * 2019/10/12 22:25
 */
object TemporaryMemory {
  val database: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile](schema.databaseName, config = ConfigFactory.load())

  private lazy val logger: HyLogger = JamContext.logger.get()

  /**
   * 记忆初始化
   *
   * @return 初始化结果
   */
  def init()(implicit exec: ExecutionContext): Future[Unit] = {
    logger.log(s"${AnsiColor.YELLOW}正在构建${JamConfig.name}的记忆...")
    database.db.run(Tables.createIfNotExist).map(_ => logger.log(s"${AnsiColor.YELLOW}记忆构建完成"))
  }
}
