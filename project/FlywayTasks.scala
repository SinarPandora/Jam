import sbt.*
import sbt.Keys.*
import scala.sys.process.*

/** Flyway migrate/clean via forked JVM (replaces Gradle flyway plugin tasks). */
object FlywayTasks {
  val flywayMigrate = taskKey[Unit]("Apply Flyway migrations")
  val flywayClean = taskKey[Unit]("Clean Flyway-managed schema")
  val recreateDatabase = taskKey[Unit]("flywayClean then flywayMigrate")
  val migrateDatabase = taskKey[Unit]("Alias for flywayMigrate")

  // sbt `config` macro requires the val name to match the config id
  lazy val Flyway: Configuration = config("Flyway").hide

  def settings(flywayVersion: String): Seq[Setting[?]] = Seq(
    ivyConfigurations += Flyway,
    libraryDependencies ++= Seq(
      "org.flywaydb" % "flyway-core" % flywayVersion % Flyway,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion % Flyway,
      "org.postgresql" % "postgresql" % "42.3.7" % Flyway,
    ),
    flywayMigrate := runFlyway(
      streams.value.log,
      (Compile / baseDirectory).value,
      Classpaths.managedJars(Flyway, classpathTypes.value, update.value).files,
      clean = false,
    ),
    flywayClean := runFlyway(
      streams.value.log,
      (Compile / baseDirectory).value,
      Classpaths.managedJars(Flyway, classpathTypes.value, update.value).files,
      clean = true,
    ),
    recreateDatabase := {
      flywayClean.value
      flywayMigrate.value
    },
    migrateDatabase := flywayMigrate.value,
  )

  private def runFlyway(log: Logger, base: File, jars: Seq[File], clean: Boolean): Unit = {
    val locations = (base / "src" / "main" / "resources" / "db" / "migration").getAbsolutePath
    val url = sys.props.getOrElse("flyway.url", "jdbc:postgresql://localhost:5432/jam_bot")
    val user = sys.props.getOrElse("flyway.user", "sinar")
    val password = sys.props.getOrElse("flyway.password", "12345678")
    val work = IO.createTemporaryDirectory
    val src = work / "FlywayRunner.java"
    val action = if (clean) "flyway.clean();" else "flyway.migrate();"
    IO.write(
      src,
      s"""
         |import org.flywaydb.core.Flyway;
         |public class FlywayRunner {
         |  public static void main(String[] args) {
         |    Flyway flyway = Flyway.configure()
         |      .dataSource(${q(url)}, ${q(user)}, ${q(password)})
         |      .locations("filesystem:" + ${q(locations)})
         |      .load();
         |    $action
         |  }
         |}
         |""".stripMargin,
    )
    val sep = _root_.java.io.File.pathSeparator
    val cp = jars.map(_.getAbsolutePath).mkString(sep)
    val javaHome = sys.props("java.home")
    val javacBin = s"$javaHome/bin/javac"
    val javaBin = s"$javaHome/bin/java"
    log.info(s"Flyway ${if (clean) "clean" else "migrate"} url=$url")
    val compileExit = Process(Seq(javacBin, "-cp", cp, src.getAbsolutePath), work).!
    if (compileExit != 0) sys.error("FlywayRunner javac failed")
    val runCp = work.getAbsolutePath + sep + cp
    val runExit = Process(Seq(javaBin, "-cp", runCp, "FlywayRunner"), work).!
    if (runExit != 0) sys.error(s"Flyway ${if (clean) "clean" else "migrate"} failed (exit $runExit)")
  }

  private def q(s: String): String =
    "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
