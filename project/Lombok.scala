import sbt.*
import sbt.Keys.*
import sbt.internal.inc.javac.{JavaCompiler as ZincJavaCompiler, JavaTools as ZincJavaTools, Javadoc as ZincJavadoc}
import xsbti.compile.Compilers

/** Lombok annotation processing for Java modules (JDK 25 / GraalVM). */
object Lombok {
  // 1.18.40+ = JDK25; pin exact jar — HyLogger etc. may pull ancient lombok onto the report
  val version = "1.18.48"

  private val jdkCompilerOpens: Seq[String] = Seq(
    "jdk.compiler/com.sun.tools.javac.api",
    "jdk.compiler/com.sun.tools.javac.code",
    "jdk.compiler/com.sun.tools.javac.comp",
    "jdk.compiler/com.sun.tools.javac.file",
    "jdk.compiler/com.sun.tools.javac.jvm",
    "jdk.compiler/com.sun.tools.javac.main",
    "jdk.compiler/com.sun.tools.javac.model",
    "jdk.compiler/com.sun.tools.javac.parser",
    "jdk.compiler/com.sun.tools.javac.processing",
    "jdk.compiler/com.sun.tools.javac.tree",
    "jdk.compiler/com.sun.tools.javac.util",
  ).map(p => s"-J--add-opens=$p=ALL-UNNAMED")

  def settings: Seq[Setting[?]] = Seq(
    libraryDependencies += "org.projectlombok" % "lombok" % version % Provided,
    dependencyOverrides += "org.projectlombok" % "lombok" % version,
    // Zinc in-process javac + Lombok fails on JDK 25; use forked javac.
    Compile / compilers := {
      val prev = (Compile / compilers).value
      val homePath = javaHome.value.map(_.toPath)
      val forkedJavac = ZincJavaCompiler.fork(javaHome = homePath)
      val forkedJavadoc = ZincJavadoc.fork(javaHome = homePath)
      val tools = ZincJavaTools(forkedJavac, forkedJavadoc)
      Compilers.of(prev.scalac(), tools)
    },
    Compile / javacOptions ++= lombokJavacOptions((Compile / update).value),
    // javadoc does not accept -processorpath / -J--add-opens
    Compile / doc / sources := Seq.empty,
    Compile / doc / javacOptions := Seq("--release", "25"),
    Test / compilers := (Compile / compilers).value,
    Test / javacOptions ++= lombokJavacOptions((Test / update).value),
  )

  private def lombokJavacOptions(report: UpdateReport): Seq[String] = {
    val jar = report
      .matching(moduleFilter(organization = "org.projectlombok", name = "lombok"))
      .find(_.getName.contains(version))
      .getOrElse(sys.error(s"lombok $version jar not found on update report"))
    jdkCompilerOpens ++ Seq("-processorpath", jar.getAbsolutePath, "-proc:full")
  }
}
