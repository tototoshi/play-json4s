val _version = "0.8.0"
val _json4sVersion = "3.5.2"
val _playVersion = "2.6.0"
val _scalatestVersion = "3.0.1"

val json4sCore = "org.json4s" %% "json4s-core" % _json4sVersion
val json4sNative = "org.json4s" %% "json4s-native" % _json4sVersion
val json4sJackson = "org.json4s" %% "json4s-jackson" % _json4sVersion
val scalatest = "org.scalatest" %% "scalatest" % _scalatestVersion

val playApi = "com.typesafe.play" %% "play" % _playVersion
val playTest = "com.typesafe.play" %% "play-test" % _playVersion

val playDependencies = Seq(playApi % "provided", playTest % "test")

val publishingSettings = Seq(
  publishMavenStyle := true,
  publishTo := _publishTo(version.value),
  publishArtifact in Test := false,
  pomExtra := _pomExtra
)

val nonPublishSettings = Seq(
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  parallelExecution in Test := false
)

val baseSettings = Seq(
  organization := "com.github.tototoshi",
  version := _version,
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.8", "2.12.2"),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"
)

lazy val api = project
  .in(file("api"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-api",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sCore
    )
  )

lazy val core = project
  .in(file("core"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-core",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sCore,
      scalatest % "test"
    )
  )
  .dependsOn(api)

lazy val testCore = project
  .in(file("test-core"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-test-core",
    libraryDependencies ++= Seq(
      playApi % "provided",
      playTest % "provided",
      json4sCore
    )
  )
  .dependsOn(core)

lazy val testNative = project
  .in(file("test-native"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-test-native",
    libraryDependencies ++= Seq(
      playApi % "provided",
      playTest % "provided",
      json4sNative
    )
  )
  .dependsOn(core, testCore)

lazy val testJackson = project
  .in(file("test-jackson"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-test-jackson",
    libraryDependencies ++= Seq(
      playApi % "provided",
      playTest % "provided",
      json4sJackson
    )
  )
  .dependsOn(core, testCore)

lazy val native = project
  .in(file("native"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-native",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sNative,
      scalatest % "test"
    )
  )
  .dependsOn(core % "test->test;compile->compile", api, testNative % "test")

lazy val jackson = project
  .in(file("jackson"))
  .settings(baseSettings)
  .settings(publishingSettings)
  .settings(
    name := "play-json4s-jackson",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sJackson,
      scalatest % "test"
    )
  )
  .dependsOn(core % "test->test;compile->compile", testJackson % "test")

lazy val example = project
  .in(file("example"))
  .enablePlugins(PlayScala)
  .settings(baseSettings)
  .settings(nonPublishSettings)
  .dependsOn(jackson)

lazy val playJson4s = project
  .in(file("."))
  .settings(baseSettings)
  .settings(nonPublishSettings)
  .settings(
    name := "json4s"
  )
  .aggregate(api, native, jackson, core, testCore, testNative, testJackson, example)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

val _pomExtra =
  <url>http://github.com/tototoshi/play-json4s</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:tototoshi/play-json4s.git</url>
    <connection>scm:git:git@github.com:tototoshi/play-json4s.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tototoshi</id>
      <name>Toshiyuki Takahashi</name>
      <url>http://tototoshi.github.io</url>
    </developer>
  </developers>
