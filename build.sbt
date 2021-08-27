import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.collection.mutable.ArrayBuilder
import scala.sys.process._
import scala.util.control.NonFatal

import org.antlr.v4.{Tool => Antlr4}
import sbt.nio.Keys._
import sbtassembly.AssemblyKeys

val scalaVer = "3.0.1"

val v = new {
  val fs2 = "3.1.0"
  val jsoup = "1.14.2"
  val catsCore = "2.6.1"
  val catsEffect = "3.2.3"
  val http4s = "1.0.0-M23"
  val circe = "0.14.1"
  val scalaCheckEffect = "1.0.2"
  val munitCatsEffect = "1.0.3"
  val antlr4 = "4.9.2"
}

lazy val operatingSystem = settingKey[OS](
  "Current operating system",
)

lazy val upxPath = settingKey[String](
  "Path to UPX binary",
)
lazy val nativeImageCompressed = taskKey[Unit](
  "Build and compress the native image",
)

inThisBuild(
  List(
    version := "1.0.0",
    scalaVersion := scalaVer,
    scalacOptions ++= Seq("-rewrite", "-indent"),
    libraryDependencies ++= Seq(
      // main dependencies
      "co.fs2" %% "fs2-core" % v.fs2,
      "co.fs2" %% "fs2-io" % v.fs2,
      "org.jsoup" % "jsoup" % v.jsoup,
      "org.typelevel" %% "cats-core" % v.catsCore,
      "org.typelevel" %% "cats-effect" % v.catsEffect,
      "org.http4s" %% "http4s-dsl" % v.http4s,
      "org.http4s" %% "http4s-blaze-client" % v.http4s,
      "io.circe" %% "circe-core" % v.circe,
      "io.circe" %% "circe-generic" % v.circe,
      "io.circe" %% "circe-parser" % v.circe,
      "io.circe" %% "circe-testing" % v.circe,
      "io.circe" %% "circe-yaml" % v.circe,
      "org.antlr" % "antlr4-runtime" % v.antlr4,
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "munit-cats-effect-3" % v.munitCatsEffect,
      "org.typelevel" %% "scalacheck-effect" % v.scalaCheckEffect,
      "org.typelevel" %% "scalacheck-effect-munit" % v.scalaCheckEffect,
    ).map(_ % Test),

    // options
    Global / onChangedBuildSource := ReloadOnSourceChanges,
  ),
)

name := "htxRoot"

val antlrBuildGrammars = taskKey[Unit](
  "Run antlr4 on some grammars",
)
val antlrClean = taskKey[Unit](
  "Clean generated antlr4 files",
)
val antlrOutputDir = settingKey[File](
  "Where to stick the java files after they are generated",
)
val antlrGrammars = settingKey[Seq[String]](
  "List of grammars to compile",
)

lazy val grammar = project
  .in(file("htx-grammar"))
  .settings(
    moduleName := "htx-grammar",
    resourceDirectory := baseDirectory.value / "src" / "main" / "resources",
    antlrOutputDir := baseDirectory.value / "src" / "main" / "java",
    antlrBuildGrammars / fileInputs += (baseDirectory / resourceDirectory).value.toGlob / "*.g4",
    cleanFiles += antlrOutputDir.value,
    Compile / compile := (Compile / compile)
      .dependsOn(antlrBuildGrammars)
      .value,
    antlrBuildGrammars := (Def.taskDyn {
      val pkg = "dev.bjb.htx.grammar"
      val log = streams.value.log
      val changes = antlrBuildGrammars.inputFileChanges
      val updated = (changes.created ++ changes.modified).toSet.size > 0

      def mkArgs(
          inFile: File,
          outDir: File,
          withPkg: Boolean = true,
      ): Array[String] = {
        val baseArgs = Array("-Xexact-output-dir", "-visitor")
        val pkgArgs = if (withPkg) Array("-package", pkg) else Array[String]()
        val outArgs = Array("-o", outDir.toString)
        val inArg = Array(inFile.toString)
        baseArgs ++ pkgArgs ++ outArgs ++ inArg
      }

      def runAntlr(
          inFile: File,
          outDir: File,
          withPkg: Boolean = true,
      ): Unit = {
        val args = mkArgs(inFile, outDir, withPkg)
        log.info(s"Running Antlr4 with options: ${args.toList}")
        val antlr = new Antlr4(args)
        antlr.processGrammarsOnCommandLine()
        if (antlr.getNumErrors() > 0)
          throw new MessageOnlyException(s"Antlr4 Failed")
      }

      val grammars = (antlrBuildGrammars / allInputFiles).value

      if (updated) Def.task {
        clean.value
        grammars.foreach { grammar =>
          val inPath = grammar.toFile
          val outPath = antlrOutputDir.value

          runAntlr(inPath, outPath / "pkg")
          runAntlr(inPath, outPath / "nopkg", withPkg = false)
        }
      }
      else Def.task {}
    }).value,
  )

lazy val core = project
  .in(file("htx-core"))
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .dependsOn(grammar)
  .settings(
    moduleName := "htx-core",
  )

lazy val cli = project
  .in(file("htx-cli"))
  .dependsOn(core)
  .enablePlugins(JavaAppPackaging, UniversalPlugin, NativeImagePlugin)
  .settings(
    moduleName := "htx-cli",
    assembly / mainClass := Some("dev.bjb.htx.cli.Main"),
    assembly / assemblyJarName := "htx.jar",
    nativeImageVersion := "21.2.0",
    nativeImageInstalled := {
      val installed = sys.env
        .get("NATIVE_IMAGE_INSTALLED")
        .map(_.toBoolean)
        .getOrElse(false)

      val inCi = sys.env
        .get("CI")
        .map(_.toBoolean)
        .getOrElse(false)

      installed || (!inCi)
    },
    nativeImageOutput := file("target") / "htx",
    nativeImageOptions ++= {
      val musl = sys.env
        .get("NATIVE_IMAGE_MUSL")
        .map(path => s"--libc=musl")
        .toSeq

      val static = sys.env
        .get("NATIVE_IMAGE_STATIC")
        .map(_.toBoolean)
        .filter(identity)
        .map(_ => "--static")
        .toSeq

      val mostlyStatic = sys.env
        .get("NATIVE_IMAGE_MOSTLY_STATIC")
        .map(_.toBoolean)
        .filter(identity)
        .map(_ => "-H:+StaticExecutableWithDynamicLibC")
        .toSeq

      musl ++ static ++ mostlyStatic
    },
    operatingSystem := OS.get,
    upxPath := {
      // TODO: rewrite all this in terms of `File` and the `/` operator
      val os = operatingSystem.value
      val (sep, suffix) = os match {
        case Windows => ("\\", "exe")
        case Linux   => ("/", "linux")
        case MacOS   => ("/", "macos")
      }
      val upx = "upx." + suffix
      List("project", "bin", "upx", upx).mkString(sep)
    },
    nativeImageCompressed := {
      nativeImage.value
      val os = operatingSystem.value
      val upx = upxPath.value

      val target = os match {
        case Windows => "target\\htx.exe"
        case _       => "target/htx"
      }

      val command = List(upx, target)
      streams.value.log.info("Running: " + command.mkString(" "))

      val exit = Process(command).!
      if (exit != 0) {
        throw new MessageOnlyException(s"non-zero exit from UPX: $exit")
      }
    },
    Compile / mainClass := Some("dev.bjb.htx.cli.Main"),
    Global / excludeLintKeys += nativeImageVersion,
  )

lazy val LiveTest = config("live") extend (Test)
def liveFilter(name: String): Boolean = name endsWith "LiveTest"
def unitFilter(name: String): Boolean =
  (name endsWith "Test") && !liveFilter(name)

addCommandAlias("testLive", "tests/LiveTest/test")
lazy val testAll = taskKey[Unit]("Run all tests")

lazy val tests = project
  .in(file("htx-tests"))
  .dependsOn(core, cli)
  .configs(LiveTest)
  .settings(
    fork := true,
    inConfig(LiveTest)(Defaults.testTasks),
    Test / parallelExecution := true,
    Test / testOptions := Seq(Tests.Filter(unitFilter)),
    LiveTest / parallelExecution := false,
    LiveTest / testOptions := Seq(Tests.Filter(liveFilter)),
    testAll := {
      (LiveTest / test).value
      (Test / test).value
    },
  )
