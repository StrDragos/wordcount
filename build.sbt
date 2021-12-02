ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val zioVersion = "1.0.12"
val zioProcessVersion = "0.5.0"
val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .settings(
    name := "wordcounter",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-process" % zioProcessVersion,
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "io.circe" %% "circe-core"           % circeVersion,
      "io.circe" %% "circe-parser"         % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
    )
  )
