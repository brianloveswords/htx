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
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "munit-cats-effect-3" % v.munitCatsEffect,
      "org.typelevel" %% "scalacheck-effect" % v.scalaCheckEffect,
      "org.typelevel" %% "scalacheck-effect-munit" % v.scalaCheckEffect,
    ).map(_ % Test),

    // options
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    Test / parallelExecution := false,
  ),
)

name := "htxRoot"

lazy val core = project
  .in(file("htx-core"))
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(
    moduleName := "htx-core",
  )

lazy val cli = project
  .in(file("htx-cli"))
  .dependsOn(core)
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(
    moduleName := "htx-cli",
  )

lazy val LiveTest = config("live") extend (Test)
def liveFilter(name: String): Boolean = name endsWith "LiveTest"
def unitFilter(name: String): Boolean =
  (name endsWith "Test") && !liveFilter(name)

lazy val tests = project
  .in(file("htx-tests"))
  .dependsOn(core, cli)
  .configs(LiveTest)
  .settings(
    inConfig(LiveTest)(Defaults.testTasks),
    fork := true,
    Defaults.itSettings,
    Test / testOptions := Seq(Tests.Filter(unitFilter)),
    LiveTest / testOptions := Seq(Tests.Filter(liveFilter)),
  )
