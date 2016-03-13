val _version = "0.5.0-SNAPSHOT"
val _json4sVersion = "3.3.0"
val _playVersion = "2.5.0"

val json4sCore = "org.json4s" %% "json4s-core" % _json4sVersion
val json4sNative = "org.json4s" %% "json4s-native" % _json4sVersion
val json4sJackson = "org.json4s" %% "json4s-jackson" % _json4sVersion
val scalatest = "org.scalatest" %% "scalatest" % "2.2.2"

val playApi = "com.typesafe.play" %% "play" % _playVersion
val playTest = "com.typesafe.play" %% "play-test" % _playVersion
val playWS = "com.typesafe.play" %% "play-ws" % _playVersion

val playDependencies = Seq(playApi % "provided", playTest % "test")

val publishingSettings = Seq(
  publishMavenStyle := true,
  publishTo <<= version { (v: String) => _publishTo(v) },
  publishArtifact in Test := false,
  pomExtra := _pomExtra
)

val baseSettings = Seq(
  organization := "com.github.tototoshi",
  version := _version,
  scalaVersion := "2.11.4",
  crossScalaVersions := scalaVersion.value :: "2.10.4" :: Nil,
  scalacOptions ++= Seq("-feature", "-deprecation"),
  resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"
)

lazy val api = Project(
  id = "play-json4s-api",
  base = file("api"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-api",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sCore
    )
  )
)

lazy val core = Project(
  id = "play-json4s-core",
  base = file("core"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-core",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sCore
    )
  )
).dependsOn(api)

lazy val testCore = Project(
  id = "play-json4s-test-core",
  base = file("test-core"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-test-core",
    libraryDependencies ++= Seq(
      playApi % "provided",
      playTest % "provided",
      json4sCore
    )
  )
).dependsOn(core)

lazy val testNative = Project(
  id = "play-json4s-test-native",
  base = file("test-native"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-test-native",
    libraryDependencies ++= Seq(
      playApi % "provided",
      playTest % "provided",
      json4sNative
    )
  )
).dependsOn(core, testCore)

lazy val testJackson = Project(
  id = "play-json4s-test-jackson",
  base = file("test-jackson"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-test-jackson",
    libraryDependencies ++= Seq(
      playApi % "provided",
      playTest % "provided",
      json4sJackson
    )
  )
).dependsOn(core, testCore)

lazy val native = Project(
  id = "play-json4s-native",
  base = file("native"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-native",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sNative,
      scalatest % "test",
      playWS % "test"
    )
  )
).dependsOn(core, api, testNative % "test", testHelper % "test")

lazy val jackson = Project(
  id = "play-json4s-jackson",
  base = file("jackson"),
  settings = baseSettings ++ publishingSettings ++ Seq(
    name := "play-json4s-jackson",
    libraryDependencies ++= playDependencies ++ Seq(
      json4sJackson,
      scalatest % "test",
      playWS % "test"
    )
  )
).dependsOn(core, testJackson % "test", testHelper % "test")

lazy val testHelper = Project(
  id = "play-json4s-test-helper",
  base = file("test-helper"),
  settings = baseSettings ++ Seq(
    name := "play-json4s-test-helper",
    libraryDependencies ++= Seq(playApi, playTest, playWS)
  )
).dependsOn(core)

lazy val playJson4s = Project(
  id = "play-json4s",
  base = file("."),
  settings = baseSettings ++ Seq(
    name := "json4s",
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    parallelExecution in Test := false
  )
).aggregate(native, jackson, core, testCore, testNative, testJackson)

lazy val example = Project(
  id = "play-json4s-example",
  base = file("example")
).enablePlugins(PlayScala)
.settings(
  scalaVersion := "2.11.8"
)
.dependsOn(jackson)

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
      <url>http://tototoshi.github.com</url>
    </developer>
  </developers>
