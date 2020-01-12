package o.lartifa.jam.database.temporary

import com.typesafe.config.ConfigFactory
import slick.jdbc.{H2Profile, SQLiteProfile}

/**
 * Author: sinar
 * 2019/10/13 19:38 
 */
package object schema {
  val (databaseType, databaseName) = if (ConfigFactory.load().getBoolean("system.debugMode")) {
    (SQLiteProfile, "databases.temporary_memory_debug")
  } else {
    (H2Profile, "databases.temporary_memory")
  }
}
