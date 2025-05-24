ThisBuild / scalaVersion := "3.7.0"

lazy val root =
  project
    .in(file("."))
    .settings(
      name := "qantica",
      scalacOptions ++= Seq("-explain"),
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
        "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.19" % Test
      )
    )
