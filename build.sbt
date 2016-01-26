SubscriptSbt.projectSettings

lazy val vkMiner = (project in file(".")).settings(
  version := "1.0.0"
, name    := "vk-miner"

, sbtVersion   := "0.13.7"
, scalaVersion := "2.11.7"

, libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.4"
  , "org.apache.httpcomponents" % "httpclient" % "4.4"
  , "org.json4s" %% "json4s-native" % "3.2.11"

  , "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
  )

, resolvers += Resolver.sonatypeRepo("snapshots")
, libraryDependencies += "org.subscript-lang" %% "subscript-swing" % "3.0.1"

, javaOptions += "-Xmx4g"
, javaOptions += "-Xms2g"

, excludeFilter := "Main.scala"
)