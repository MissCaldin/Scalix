ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "Scalix"
  )

libraryDependencies += "org.json4s" %% "json4s-ast" % "4.0.7"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.1.0-M8"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.6"