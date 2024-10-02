import sbt.librarymanagement.CrossVersion

lazy val scala2 = "2.13.14"
lazy val scala3 = "3.5.1"
lazy val supportedScalaVersions = List(scala2, scala3)

ThisBuild / organization := "simex"

ThisBuild / version := "0.8.0" // Keep this in sync with simex-messaging

lazy val commonSettings = Seq(
  scalaVersion := scala3,
  libraryDependencies ++= Dependencies.all,
  //resolvers += Resolver.githubPackages("TheDiscProg"),
  githubOwner := "TheDiscProg",
  githubRepository := "simex-caching"
)

lazy val root = project.in(file("."))
  .enablePlugins(
    ScalafmtPlugin
  )
  .settings(
    commonSettings,
    name := "simex-caching",
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

githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")

addCommandAlias("formatAll", ";scalafmt;test:scalafmt;integrationTest/test:scalafmt;")
addCommandAlias("cleanAll", ";clean;integrationTest:clean")
addCommandAlias("itTest", ";integrationTest/test:test")
addCommandAlias("testAll", ";cleanAll;formatAll;test;itTest;")

