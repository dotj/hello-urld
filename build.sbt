lazy val akkaHttpVersion = "10.2.7"
lazy val akkaVersion = "2.6.18"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.github.dotj",
      scalaVersion := "2.13.4"
    )
  ),
  name := "hello-urld",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.10",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
    "com.typesafe.slick" %% "slick" % "3.3.3",
    "org.postgresql" % "postgresql" % "42.3.1",
    "com.zaxxer" % "HikariCP" % "5.0.1",
    "org.slf4j" % "slf4j-nop" % "1.7.32"
  )
)
