lazy val p = (project in file(".")).settings(
  sbtVersion   := "0.13.7"
, scalaVersion := "2.11.7"

, initialCommands := """
    |import vkminer._
    |
    |import vkminer.dom._
    |import vkminer.serialize._
    |import vkminer.strategies._
    |// import Main._
    |
    |import org.json4s._
    |import org.json4s.native.JsonMethods._
    |import org.json4s.JsonDSL._
    |
    |import scala.xml._
    |
    |val token = "4469ea62706d65df37a86c02807ca7b2147091a56df92e9cf3fb826719030382fc02aeb31a3cd0f72202f"
    |// val gb = new GraphBuilder(api)
    |
    |val e = new VkEnvironment with XmlSerializerComponent
    |                          with GexfSerializerComponent
    |                          with UniversitiesSerializerComponent {
    |  val workingDirectory = "/Users/anatolii/Desktop"
    |  val api = new VkApi(token)
    |  val universities = UniversitiesSerializer.deserialize("universities")
    |}
    |import e._
    |val api = new VkApi(token)
    |
    |val ego = new FullEgoGroup[e.type](e)
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

, excludeFilter := "Main.scala"
)