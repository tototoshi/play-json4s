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
    id = "json4s-core-play-module",
    base = file("core"),
    settings = Project.defaultSettings ++ Seq(
      name := "json4s-core-play-module",
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

  lazy val test = Project(
    id = "json4s-test-play-module",
    base = file("test"),
    settings = Project.defaultSettings ++ Seq(
      name := "json4s-test-play-module",
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
  )

  lazy val native = Project(
    id = "json4s-native-play-module",
    base = file("native"),
    settings = Project.defaultSettings ++ Seq(
      name := "json4s-native-play-module",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= playDependencies ++ Seq(
        json4sNative
      ),
      resolvers += typesafeRepo
    )
  ).dependsOn(core, test % "test")

  lazy val jackson = Project(
    id = "json4s-jackson-play-module",
    base = file("jackson"),
    settings = Project.defaultSettings ++ Seq(
      name := "json4s-jackson-play-module",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= playDependencies ++ Seq(
        json4sJackson
      ),
      resolvers += typesafeRepo
    )
<<<<<<< Updated upstream
  ).dependsOn(core, test % "test")
=======
  ).dependsOn(core, test % "test->compile")
>>>>>>> Stashed changes

  lazy val json4sPlayModule = Project(
    id = "json4s-play-module",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "json4s-play-module",
      organization := "com.github.tototoshi",
      version := _version,
      scalaVersion := _scalaVersion,
      scalacOptions ++= Seq("-feature"),
      resolvers += typesafeRepo,
      publishArtifact := false,
      publish := {},
      publishLocal := {}
    )
  ).aggregate(native, jackson, core, test)
}
