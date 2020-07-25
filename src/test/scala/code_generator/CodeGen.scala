package code_generator

import slick.codegen.SourceCodeGenerator

/**
 * Author: sinar
 * 2019/11/3 13:44
 */
object CodeGen {
  final val profile: String = "slick.jdbc.PostgresProfile"
  final val driver: String = "org.postgresql.Driver"

  def url: String = s"jdbc:postgresql://localhost:5432/jam_bot"

  def main(args: Array[String]): Unit = {
    SourceCodeGenerator.run(
      profile = profile,
      jdbcDriver = driver,
      url = url,
      outputDir = "/Users/sinar/IdeaProjects/Jam/src/main/scala",
      pkg = s"o.lartifa.jam.database.temporary.codeGen.gen",
      user = Some("sinar"),
      password = None,
      ignoreInvalidDefaults = true,
      outputToMultipleFiles = true)
  }
}
