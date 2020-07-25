package o.lartifa.jam.database.temporary

import com.typesafe.config.ConfigFactory
import slick.jdbc.{H2Profile, PostgresProfile}

/**
 * Author: sinar
 * 2019/10/13 19:38
 */
package object schema {
  val (databaseType, databaseName) =
    ConfigFactory.load().getString("databases.use") match {
      case "PGSQL" => (PostgresProfile, "databases.temporary_memory_PGSQL")
      case "H2" => (H2Profile, "databases.temporary_memory_H2")
      case other => throw new IllegalArgumentException(s"不支持的数据库类型：$other")
    }
}
