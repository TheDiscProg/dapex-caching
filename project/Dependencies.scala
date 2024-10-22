import sbt._

object Dependencies {

  private lazy val simexVersion = "0.9.3"
  private lazy val circeVersion = "0.14.10"
  private lazy val catsEffectVersion = "3.5.4"
  private lazy val hazelcastVersion = "5.5.0"

  lazy val all = Seq(
    "io.github.thediscprog" %% "simex-messaging" % simexVersion,
    "com.hazelcast" % "hazelcast" % hazelcastVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-config" % "0.10.1",
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
    "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
    "ch.qos.logback" % "logback-classic" % "1.5.8" % Test
  )
}
