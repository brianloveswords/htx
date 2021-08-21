val scala3Version = "3.0.1"

lazy val main = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "scala-dev-template",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.2.3",

      // test
      "org.typelevel" %% "scalacheck-effect" % "1.0.2" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.2" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.3" % Test,
    ),
  )

lazy val docs = project
  .in(file("main-docs"))
  .dependsOn(main)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scala3Version,
  )
