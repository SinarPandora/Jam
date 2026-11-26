import sbt.*
import sbt.Keys.*

ThisBuild / organization := "lartifa.o"
ThisBuild / version := "4.2.0"
ThisBuild / scalaVersion := "3.9.0"
ThisBuild / javacOptions ++= Seq("--release", "25")
// 3.0-migration：保留 2.13 写法可编译，并把差异报成警告（json4s Manifest、package object）
ThisBuild / scalacOptions ++= Seq("-source:3.0-migration")
ThisBuild / resolvers += "jitpack" at "https://jitpack.io"
// Packaging does not need javadoc/scaladoc artifacts (lombok -processorpath breaks javadoc)
ThisBuild / Compile / packageDoc / publishArtifact := false
Global / excludeLintKeys += packageDoc / publishArtifact

val pekkoVersion = "1.1.3"
val slickVersion = "3.5.2"
val groovyVersion = "3.0.25"
val scalatestVersion = "3.2.19"
val flywayVersion = "11.7.2"

lazy val installDist = taskKey[File]("Install app like Gradle installDist")

lazy val commonTestSettings: Seq[Setting[?]] = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "org.scalatestplus" %% "junit-4-13" % s"$scalatestVersion.0" % Test,
    "junit" % "junit" % "4.13.2" % Test,
    "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  ),
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
)

lazy val javaLibSettings: Seq[Setting[?]] = Seq(
  crossPaths := false,
  autoScalaLibrary := false,
) ++ Lombok.settings ++ Seq(
  libraryDependencies += "junit" % "junit" % "4.13.2" % Test,
  libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
)

lazy val one_bot_protocol = (project in file("one_bot_protocol"))
  .settings(javaLibSettings*)
  .settings(
    name := "one_bot_protocol",
    libraryDependencies ++= Seq(
      "cn.hutool" % "hutool-all" % "5.8.2",
      "com.github.hydevelop" % "HyLogger" % "4.0.254",
    ),
  )

lazy val picq_bot_x_fork = (project in file("picq_bot_x_fork"))
  .settings(javaLibSettings*)
  .settings(
    name := "picq_bot_x_fork",
    libraryDependencies ++= Seq(
      "cn.hutool" % "hutool-all" % "5.8.2",
      "com.github.hydevelop" % "HyLogger" % "4.0.254",
      "com.google.code.gson" % "gson" % "2.11.0",
    ),
  )

lazy val jam_utils = (project in file("jam_utils"))
  .settings(commonTestSettings*)
  .settings(
    name := "jam_utils",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
      "com.github.pathikrit" %% "better-files" % "3.9.2",
      "org.eclipse.jgit" % "org.eclipse.jgit" % "5.13.0.202109080827-r",
    ),
  )

lazy val jam_self = (project in file("jam_self"))
  .dependsOn(picq_bot_x_fork, jam_utils)
  .enablePlugins(JavaAppPackaging)
  .settings(commonTestSettings*)
  .settings(FlywayTasks.settings(flywayVersion)*)
  .settings(
    name := "jam_self",
    Compile / mainClass := Some("o.lartifa.jam.Bootloader"),
    Compile / discoveredMainClasses := Seq("o.lartifa.jam.Bootloader"),
    executableScriptName := "jam_self",
    Test / sources := (Test / sources).value.filterNot(_.getName == "Playground.scala"),
    excludeDependencies += ExclusionRule("ch.qos.logback", "logback-classic"),
    libraryDependencies ++= Seq(
      "org.reflections" % "reflections" % "0.10.2",
      "net.redhogs.cronparser" % "cron-parser-core" % "3.5",
      "org.apache.pekko" %% "pekko-actor" % pekkoVersion,
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-codegen" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.h2database" % "h2" % "2.1.212",
      "org.postgresql" % "postgresql" % "42.3.7",
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion,
      "org.jsoup" % "jsoup" % "1.15.3",
      "com.jayway.jsonpath" % "json-path" % "2.7.0",
      "org.codehaus.groovy" % "groovy-all" % groovyVersion,
      "org.apache.httpcomponents.client5" % "httpclient5" % "5.1.3",
      "org.freemarker" % "freemarker" % "2.3.31",
      "com.lihaoyi" %% "requests" % "0.9.0",
      "org.json4s" %% "json4s-jackson" % "4.0.7",
      "com.bladecoder.ink" % "blade-ink" % "1.0.0",
      "com.sksamuel.scrimage" % "scrimage-core" % "4.0.30",
      "com.sksamuel.scrimage" % "scrimage-filters" % "4.0.31",
      "com.github.kokorin.jaffree" % "jaffree" % "0.11.0",
      "com.github.houbb" % "pinyin" % "0.4.0",
      "com.github.hydevelop" % "HyLogger" % "4.0.254",
      "org.slf4j" % "slf4j-simple" % "1.7.36",
    ),
    Universal / javaOptions ++= Seq(
      "-J-Xmx1024m",
      "-J-Xms512m",
      "-J-ea",
      "-J-server",
      "-Dfile.encoding=UTF-8",
    ),
    bashScriptExtraDefines += """app_classpath="$app_classpath:$app_home/plugins/*"""",
    batScriptExtraDefines += """set "APP_CLASSPATH=%APP_CLASSPATH%;%APP_HOME%\plugins\*"""",
    Universal / mappings ++= {
      val converter = fileConverter.value
      val confBase = baseDirectory.value / "conf"
      if (!confBase.exists) Seq.empty
      else {
        val files = (confBase ** "*").get().filter(_.isFile)
        files
          .filterNot { f =>
            val n = f.getName
            n.contains("test") || n.contains("local")
          }
          .map { f =>
            val rel = confBase.toURI.relativize(f.toURI).getPath
            converter.toVirtualFile(f.toPath) -> s"conf/$rel"
          }
      }
    },
    // Unrolled: `.value` cannot sit inside a dynamic map over project ids
    Universal / mappings ++= {
      val jars = Seq(
        (LocalProject("meme_maker") / Compile / packageBin).value,
        (LocalProject("meme_maker_v1") / Compile / packageBin).value,
        (LocalProject("cool_apis") / Compile / packageBin).value,
        (LocalProject("poket_memo") / Compile / packageBin).value,
        (LocalProject("story_runner") / Compile / packageBin).value,
      )
      jars.map(j => j -> s"plugins/${j.name}")
    },
    installDist := Def.uncached {
      val staged = (Universal / stage).value
      // 固定在模块 target/install，避免 sbt 2 的 target/out/... 布局改掉 verify-jam 路径
      val dest = baseDirectory.value / "target" / "install" / name.value
      IO.delete(dest)
      IO.copyDirectory(staged, dest, overwrite = true, preserveLastModified = true)
      streams.value.log.info(s"Installed distribution to $dest")
      dest
    },
  )

def jamPlugin(id: String): Project =
  Project(id, file(s"plugins/$id"))
    .dependsOn(jam_self)
    .settings(commonTestSettings*)
    .settings(
      name := id,
      version := "0.1",
      Test / sources := (Test / sources).value.filterNot(_.getName == "Playground.scala"),
    )

lazy val meme_maker = jamPlugin("meme_maker")
lazy val meme_maker_v1 = jamPlugin("meme_maker_v1")
lazy val cool_apis = jamPlugin("cool_apis")
lazy val poket_memo = jamPlugin("poket_memo")
lazy val story_runner = jamPlugin("story_runner")

lazy val root = (project in file("."))
  .aggregate(
    one_bot_protocol,
    picq_bot_x_fork,
    jam_utils,
    jam_self,
    meme_maker,
    meme_maker_v1,
    cool_apis,
    poket_memo,
    story_runner,
  )
  .settings(
    name := "Jam",
    publish / skip := true,
  )
