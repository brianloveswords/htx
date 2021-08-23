val scala3Version = "3.0.1"

val circeVersion = "0.14.1"
val fs2Version = "3.1.0"

lazy val main = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "mdlink",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "com.github.pureconfig" %% "pureconfig-core" % "0.16.0",
      "org.jsoup" % "jsoup" % "1.14.2",
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.2.3",
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
  )

lazy val docs = project
  .in(file("main-docs"))
  .dependsOn(main)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scala3Version,
  )
