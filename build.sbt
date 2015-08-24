lazy val p = (project in file(".")).settings(
  sbtVersion   := "0.13.7"
, scalaVersion := "2.11.7"

, initialCommands := """
    |import vkminer._
    |import Main._
    |
    |import org.json4s._
    |import org.json4s.native.JsonMethods._
    |import org.json4s.JsonDSL._
    |
    |import scala.xml._
    |
    |val token = "8e5dbd93029a09d3e725e2110a2357a791e23eae30d66b5a9e44422eb9cd6115f525ab4f8bfc4fcd19d34"
    |val api = new VkApi(token)
    |val gb = new GraphBuilder(api)
  """.stripMargin

, libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.4",
    "org.apache.httpcomponents" % "httpclient" % "4.4",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "org.apache.spark" %% "spark-mllib" % "1.2.1"

  , "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
  )

, javaOptions += "-Xmx4g"
, javaOptions += "-Xms2g"
)