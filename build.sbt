val scala3Version = "3.0.1"

val circeVersion = "0.14.1"
val fs2Version = "3.1.0"
val http4sVersion = "1.0.0-M23"

val graalConfigPath = "src/graal"

lazy val main = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "mdlink",
    version := "0.1.0",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-rewrite", "-indent"),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.github.pureconfig" %% "pureconfig-core" % "0.16.0",
      "org.jsoup" % "jsoup" % "1.14.2",
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.2.3",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-testing" % circeVersion,
      "io.circe" %% "circe-yaml" % circeVersion,

      // test
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.3" % Test,
      "org.typelevel" %% "scalacheck-effect" % "1.0.2" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.2" % Test,
    ),
    // necessary for building native images
    fork := true,
    javaOptions += s"-agentlib:native-image-agent=config-output-dir=$graalConfigPath",

    // options
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    Universal / javaOptions ++= Seq(s"-no-version-check"),
    Test / parallelExecution := false,
  )

lazy val docs = project
  .in(file("main-docs"))
  .dependsOn(main)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scala3Version,
  )
