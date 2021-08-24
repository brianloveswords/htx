import java.io.File
import sbtassembly.AssemblyKeys
import scala.sys.process._

//
// Static Config
//
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
}

val graalConfigPath = "src/graal"

lazy val nativeBuild = inputKey[Unit]("Build a native image")
lazy val nativePrep = inputKey[Unit]("Test & run app with tracing agent")
lazy val debug = taskKey[Unit]("debug tasks")

//
// Task Keys
//
lazy val main = project
  .in(file("."))
  .configs(IntegrationTest)
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(
    name := "mdlink",
    version := "0.1.0",
    scalaVersion := scalaVer,
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
      "org.typelevel" %% "munit-cats-effect-3" % v.munitCatsEffect,
      "org.typelevel" %% "scalacheck-effect" % v.scalaCheckEffect,
      "org.typelevel" %% "scalacheck-effect-munit" % v.scalaCheckEffect,
    ),

    // options
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    Universal / javaOptions ++= Seq(s"-no-version-check"),
    Test / parallelExecution := false,

    // native image config
    Defaults.itSettings,
    fork := true,
    javaOptions += s"-agentlib:native-image-agent=config-merge-dir=$graalConfigPath",
    nativePrep := Def
      .sequential(
        Test / test,
        IntegrationTest / test,
      )
      .value,
    nativeBuild := {
      // dependencies
      (Test / test).value
      (IntegrationTest / test).value
      (AssemblyKeys.assembly).value
      val appName = name.value
      val appVer = version.value
      val scalaVer = scalaVersion.value

      // body
      val output = s"bin/$appName.original"
      val packed = s"bin/$appName"
      val params = Seq(
        s"-H:ReflectionConfigurationFiles=$graalConfigPath/reflect-config.json",
        "-H:+AllowIncompleteClasspath",
        "-dsa",
        "--enable-url-protocols=http,https",
        "--no-fallback",
        "-jar",
        s"target/scala-$scalaVer/$appName-assembly-$appVer.jar",
        s"$output",
      )
      s"native-image ${params.mkString(" ")}".!!
      s"upx $output -o $packed".!!
    },
  )

lazy val docs = project
  .in(file("main-docs"))
  .dependsOn(main)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scalaVer,
  )
