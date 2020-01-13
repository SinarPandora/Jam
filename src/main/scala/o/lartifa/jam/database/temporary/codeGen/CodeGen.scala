package o.lartifa.jam.database.temporary.codeGen

import slick.codegen.SourceCodeGenerator

/**
 * Author: sinar
 * 2019/11/3 13:44
 */
object CodeGen {
  final val profile: String = "slick.jdbc.H2Profile"
  final val driver: String = "org.h2.Driver"

  def url: String = s"jdbc:h2:./db/temporary_memory"

  def main(args: Array[String]): Unit = {
    SourceCodeGenerator.run(
      profile = profile,
      jdbcDriver = driver,
      url = url,
      outputDir = "/Users/sinar/IdeaProjects/Jam/src/main/scala",
      pkg = s"o.lartifa.jam.database.temporary.codeGen.gen",
      user = None,
      password = None,
      ignoreInvalidDefaults = true,
      outputToMultipleFiles = true)
  }
}
