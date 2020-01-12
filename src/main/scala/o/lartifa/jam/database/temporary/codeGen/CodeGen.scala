package o.lartifa.jam.database.temporary.codeGen

import slick.codegen.SourceCodeGenerator

/**
 * Author: sinar
 * 2019/11/3 13:44
 */
object CodeGen {
  final val profile: String = "slick.jdbc.SQLiteProfile"
  final val driver: String = "org.sqlite.JDBC"

  def url: String = s"jdbc:sqlite:db/example.sqlite"

  def main(args: Array[String]): Unit = {
    SourceCodeGenerator.run(
      profile = profile,
      jdbcDriver = driver,
      url = url,
      outputDir = "/Users/sinar/IdeaProjects/Jam/src/main/scala",
      pkg = s"o.lartifa.jam.database.temporary.schema",
      user = None,
      password = None,
      ignoreInvalidDefaults = true,
      outputToMultipleFiles = true)
  }
}
