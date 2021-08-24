import java.io.File
import sbtassembly.AssemblyKeys
import scala.sys.process._

//
// Static Config
//

val scala3Version = "3.0.1"
val appVersion = "0.1.0"
val appName = "mdlink"

val v = new {
  val fs2 = "3.1.0"
  val jsoup = "1.14.2"
  val catsCore = "2.6.1"
  val catsEffect = "3.2.3"
  val http4s = "1.0.0-M23"
  val circe = "0.14.1"
  val scalaCheckEffect = "1.0.2"
  val munitCatsEffect = "1.0.3"
}

val graalConfigPath = "src/graal"

//
// Task Keys
//
lazy val nativeImage = inputKey[Unit]("Build a native image")
lazy val nativeImagePrep = inputKey[Unit]("Test & run app with tracing agent")

lazy val main = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := appName,
    version := appVersion,
    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-rewrite", "-indent"),
    libraryDependencies ++= Seq(
      // main
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

      // test
      "org.typelevel" %% "munit-cats-effect-3" % v.munitCatsEffect % Test,
      "org.typelevel" %% "scalacheck-effect" % v.scalaCheckEffect % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % v.scalaCheckEffect % Test,
    ),

    // necessary for building native images
    fork := true,
    javaOptions += s"-agentlib:native-image-agent=config-merge-dir=$graalConfigPath",

    // options
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    Universal / javaOptions ++= Seq(s"-no-version-check"),
    Test / parallelExecution := false,

    // custom tasks
    nativeImagePrep := Def
      .sequential(
        (Test / test),
        (Compile / run).toTask(" http://example.com"),
      )
      .value,
    nativeImage := {
      // dependencies
      nativeImagePrep.evaluated
      (AssemblyKeys.assembly).value

      // body
      val packed = s"bin/$appName.original"
      val unpacked = s"$packed.original"
      val params = Seq(
        s"-H:ReflectionConfigurationFiles=$graalConfigPath/reflect-config.json",
        "-H:+AllowIncompleteClasspath",
        "-dsa",
        "--enable-url-protocols=http,https",
        "--no-fallback",
        "-jar",
        s"target/scala-$scala3Version/$appName-assembly-$appVersion.jar",
        s"bin/$appName.original",
      )
      val binDir = new File("bin")
      IO.delete(binDir)
      IO.createDirectory(binDir)
      s"native-image ${params.mkString(" ")}".!!
      s"upx $unpacked -o $packed".!!
    },
  )

lazy val docs = project
  .in(file("main-docs"))
  .dependsOn(main)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scala3Version,
  )
