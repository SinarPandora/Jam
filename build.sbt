name := "Jam"

version := "3.0"

scalaVersion := "2.13.1"

enablePlugins(JavaServerAppPackaging)

maintainer := "lartifa.o"

resolvers += "jitpack.io" at "https://jitpack.io"

// Core
libraryDependencies += "com.github.hydevelop" %% "PicqBotX" % "4.12.0.1019.PRE"

// Meta
libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.4.0"

// Database
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.2"
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.3.2"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2"
libraryDependencies += "com.h2database" % "h2" % "1.4.199"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.28.0"

// Tools
libraryDependencies += "io.reactivex.rxjava3" % "rxjava" % "3.0.0-RC7"
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "1.7.1"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
libraryDependencies += "com.vdurmont" % "emoji-java" % "4.0.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.7"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.28"
libraryDependencies += "com.lihaoyi" %% "requests" % "0.2.0"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.1"
libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.10.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"

// Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0-M1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-M1" % "test"

javaOptions in Universal ++= Seq(
  "-Dconfig.file=../conf/system.conf"
)

//enablePlugins(SbtProguard)
//
//// to configure proguard for scala, see
//// http://proguard.sourceforge.net/manual/examples.html#scala
//proguardOptions in Proguard ++= Seq(
//  "-dontoptimize",
//  "-dontnote",
//  "-dontwarn",
//  "-ignorewarnings",
//  // ...
//)
//
//// specify the entry point for a standalone app
//proguardOptions in Proguard += ProguardOptions.keepMain("o.lartifa.arfies.Bootloader")
//
//proguardVersion in Proguard := "6.0.3"
//
//// filter out jar files from the list of generated files, while
//// keeping non-jar output such as generated launch scripts
//mappings in Universal := (mappings in Universal).value.
//  filter {
//    case (file, name) => !name.endsWith(".jar")
//  }
//
//// ... and then append the jar file emitted from the proguard task to
//// the file list
//mappings in Universal ++= (proguard in Proguard).
//  value.map(jar => jar -> ("lib/" + jar.getName))
//
//// point the classpath to the output from the proguard task
//scriptClasspath := (proguard in Proguard).value.map(jar => jar.getName)
