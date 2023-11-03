ThisBuild / organization := "DAPEX"

ThisBuild / version := "0.1.2"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.10",
  libraryDependencies ++= Dependencies.all,
  resolvers += Resolver.githubPackages("TheDiscProg"),
  githubOwner := "TheDiscProg",
  githubRepository := "dapex-caching",
  addCompilerPlugin(
    ("org.typelevel" %% "kind-projector" % "0.13.2").cross(CrossVersion.full)
  ),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

lazy val root = project.in(file("."))
  .enablePlugins(
    ScalafmtPlugin
  )
  .settings(
    commonSettings,
    name := "dapex-caching",
    scalacOptions ++= Scalac.options
  )

lazy val integrationTest = (project in file ("it"))
  .enablePlugins(ScalafmtPlugin)
  .settings(
    commonSettings,
    name := "dapex-kafka-integration-test",
    publish / skip := true,
    libraryDependencies ++= Dependencies.it,
    parallelExecution := false
  )
  .dependsOn(root % "test->test; compile->compile")
  .aggregate(root)

githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")

addCommandAlias("formatAll", ";scalafmt;test:scalafmt;integrationTest/test:scalafmt;")
addCommandAlias("cleanAll", ";clean;integrationTest:clean")
addCommandAlias("itTest", ";integrationTest/test:test")
addCommandAlias("testAll", ";cleanAll;formatAll;test;itTest;")

