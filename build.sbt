name := "Jam"

version := "3.0.1"

scalaVersion := "2.13.3"

enablePlugins(JavaServerAppPackaging)

maintainer := "lartifa.o"

resolvers += "jitpack.io" at "https://jitpack.io"
resolvers += "jcenter" at "https://jcenter.bintray.com"

// Core
libraryDependencies += "com.github.hydevelop" %% "PicqBotX" % "4.15.0.1058"

// Meta
libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.4.0"
libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "3.0.4" pomOnly()

// Database
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.2"
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.3.2"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2"
libraryDependencies += "com.h2database" % "h2" % "1.4.200"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.32.3"

// Tools
libraryDependencies += "io.reactivex.rxjava3" % "rxjava" % "3.0.4"
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "2.1.4"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.1"
libraryDependencies += "com.vdurmont" % "emoji-java" % "5.1.1"
libraryDependencies += "com.lihaoyi" %% "requests" % "0.6.2"
libraryDependencies += "com.jsoniter" % "jsoniter" % "0.9.23"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.3"
libraryDependencies += "org.reflections" % "reflections" % "0.9.12"
libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.10.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"

// Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"
libraryDependencies += "org.scalamock" %% "scalamock" % "4.4.0" % Test

javaOptions in Universal ++= Seq(
  "-Dconfig.file=../conf/bot.conf"
)

scalacOptions ++= Seq(
  "-deprecation"
)
