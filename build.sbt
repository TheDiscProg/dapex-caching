import sbt.librarymanagement.CrossVersion
import sbt.url
import xerial.sbt.Sonatype.{GitHubHosting, sonatypeCentralHost}

lazy val scala2 = "2.13.14"
lazy val scala3 = "3.5.1"
lazy val supportedScalaVersions = List(scala2, scala3)

lazy val commonSettings = Seq(
  scalaVersion := scala3,
  libraryDependencies ++= Dependencies.all
)

lazy val root = project.in(file("."))
  .enablePlugins(
    ScalafmtPlugin
  )
  .settings(
    commonSettings,
    name := "simex-cluster-caching",
    scalacOptions ++= Scalac.options,
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2,13)) => Seq("-Ytasty-reader")
        case _ => Seq("-Yretain-trees")
      }
    },
    crossScalaVersions := supportedScalaVersions,
    // Scala 2 libraries to use for Scala 3
    libraryDependencies ++= Seq(
      ("com.github.pureconfig" %% "pureconfig" % "0.17.7").cross(CrossVersion.for3Use2_13)
    )
  )

lazy val integrationTest = (project in file ("it"))
  .enablePlugins(ScalafmtPlugin)
  .settings(
    commonSettings,
    name := "simex-caching-integration-test",
    publish / skip := true,
    parallelExecution := false,
    libraryDependencies ++= Seq(
      ("com.dimafeng" %% "testcontainers-scala-scalatest" % "0.41.0").cross(CrossVersion.for3Use2_13)
    )
  )
  .dependsOn(root % "test->test; compile->compile")
  .aggregate(root)

ThisBuild / version := "0.10.0"
ThisBuild / organization := "io.github.thediscprog"
ThisBuild / organizationName := "thediscprog"
ThisBuild / organizationHomepage := Some(url("https://github.com/TheDiscProg"))

ThisBuild / description := "Cluster Caching Service for Simex messages"

// Sonatype/Maven Publishing
ThisBuild / publishMavenStyle := true
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeProfileName := "io.github.thediscprog"
ThisBuild / licenses := List("GNU-3.0" -> url("https://www.gnu.org/licenses/gpl-3.0.en.html"))
ThisBuild / homepage := Some(url("https://github.com/TheDiscProg/simex-cluster-caching"))
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("TheDiscProg", "simex-cluster-caching", "TheDiscProg@gmail.com"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/TheDiscProg/simex-cluster-caching"),
    "scm:git@github.com:thediscprog/simex-cluster-caching.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "thediscprog",
    name = "TheDiscProg",
    email = "TheDiscProg@gmail.com",
    url = url("https://github.com/TheDiscProg")
  )
)

usePgpKeyHex("FC6901A47E5DA2533DCF25D51615DCC33B57B2BF")

sonatypeCredentialHost := "central.sonatype.com"
sonatypeRepository := "https://central.sonatype.com/api/v1/publisher/"

ThisBuild / versionScheme := Some("early-semver")


// The tests in the following will be in the scalaVersion setting
addCommandAlias("formatAll", ";scalafmt;test:scalafmt;integrationTest/test:scalafmt;")
addCommandAlias("cleanAll", ";clean;integrationTest:clean")
addCommandAlias("itTest", ";integrationTest/test:test")
addCommandAlias("testAll", ";cleanAll;formatAll;test;itTest;")

