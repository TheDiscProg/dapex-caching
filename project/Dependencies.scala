import sbt._

object Dependencies {

  private lazy val simexVersion = "0.8.0"

  lazy val all = Seq(
    "io.github.thediscprog" %% "simex-messaging" % simexVersion,
    "io.circe" %% "circe-core" % "0.14.5",
    "io.circe" %% "circe-generic" % "0.14.5",
    "io.circe" %% "circe-parser" % "0.14.5",
    "io.circe" %% "circe-config" % "0.10.0",
    "org.typelevel" %% "cats-effect" % "3.4.8",
    "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
    "com.hazelcast" % "hazelcast" % "5.3.1",
    "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
    "ch.qos.logback" % "logback-classic" % "1.4.11" % Test
  )
}
