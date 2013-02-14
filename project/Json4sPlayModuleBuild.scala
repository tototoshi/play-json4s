import sbt._
import sbt.Keys._

object Json4sPlayModuleBuild extends Build {

  val _version = "0.1.0"
  val _scalaVersion = "2.10.0"
  val _json4sVersion = "3.1.0"

  val json4sCore = "org.json4s" %% "json4s-core" % _json4sVersion
  val json4sNative = "org.json4s" %% "json4s-native" % _json4sVersion
  val json4sJackson = "org.json4s" %% "json4s-jackson" % _json4sVersion

  val playApi = "play" %% "play" % "2.1.0"
  val playTest = "play" %% "play-test" % "2.1.0"

  val playDependencies = Seq(playApi % "provided", playTest % "test")

  val typesafeRepo = "typesafe" at "http://repo.typesafe.com/typesafe/releases"


  lazy val core = Project(
    id = "play-json4s-core",
    base = file("core"),
    settings = Project.defaultSettings ++ Seq(
      name := "play-json4s-core",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= playDependencies ++ Seq(
        json4sCore
      ),
      resolvers += typesafeRepo
    )
  )

  lazy val testCore = Project(
    id = "play-json4s-test-core",
    base = file("test-core"),
    settings = Project.defaultSettings ++ Seq(
      name := "play-json4s-test-core",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= Seq(
        playApi % "provided",
        playTest % "provided",
        json4sCore
      ),
      resolvers += typesafeRepo
    )
  ).dependsOn(core)

  lazy val testNative = Project(
    id = "play-json4s-test-native",
    base = file("test-native"),
    settings = Project.defaultSettings ++ Seq(
      name := "play-json4s-test-native",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= Seq(
        playApi % "provided",
        playTest % "provided",
        json4sNative
      ),
      resolvers += typesafeRepo
    )
  ).dependsOn(core, testCore)

  lazy val testJackson = Project(
    id = "play-json4s-test-jackson",
    base = file("test-jackson"),
    settings = Project.defaultSettings ++ Seq(
      name := "play-json4s-test-jackson",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= Seq(
        playApi % "provided",
        playTest % "provided",
        json4sJackson
      ),
      resolvers += typesafeRepo
    )
  ).dependsOn(core, testCore)

  lazy val native = Project(
    id = "play-json4s-native",
    base = file("native"),
    settings = Project.defaultSettings ++ Seq(
      name := "play-json4s-native",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= playDependencies ++ Seq(
        json4sNative
      ),
      resolvers += typesafeRepo
    )
  ).dependsOn(core, testNative % "test")

  lazy val jackson = Project(
    id = "play-json4s-jackson",
    base = file("jackson"),
    settings = Project.defaultSettings ++ Seq(
      name := "play-json4s-jackson",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= playDependencies ++ Seq(
        json4sJackson
      ),
      resolvers += typesafeRepo
    )
  ).dependsOn(core, testJackson % "test")

  lazy val json4sPlayModule = Project(
    id = "json4s",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "json4s",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      resolvers += typesafeRepo,
      publishArtifact := false,
      publish := {},
      publishLocal := {}
    )
  ).aggregate(native, jackson, core, testCore, testNative, testJackson)
}
