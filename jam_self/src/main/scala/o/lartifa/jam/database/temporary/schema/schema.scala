package o.lartifa.jam.database.temporary

import com.typesafe.config.Config
import o.lartifa.jam.common.config.botConfigFile
import slick.jdbc.{H2Profile, PostgresProfile}

/**
 * Author: sinar
 * 2019/10/13 19:38
 */
package object schema {
  private val config: Config = botConfigFile.getConfig("databases")
  val (databaseType, name, url, user, password) =
    config.getString("use") match {
      case "PGSQL" =>
        (PostgresProfile, "databases.PGSQL",
          config.getString("PGSQL.db.url"),
          config.getString("PGSQL.db.user"),
          config.getString("PGSQL.db.password"))
      case "H2" =>
        (H2Profile, "databases.H2",
          config.getString("H2.db.url"), "root", "")
      case other => throw new IllegalArgumentException(s"不支持的数据库类型：$other")
    }
}
