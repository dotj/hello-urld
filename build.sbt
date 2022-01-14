lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "hello-urld",
    version := "0.1.0",
    scalaVersion := "2.13.6",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    },
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "com.h2database" % "h2" % "2.0.206",
      specs2 % Test,
      "org.scalatest" %% "scalatest" % "3.2.9" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
