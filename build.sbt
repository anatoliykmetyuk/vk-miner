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
    |val token = "c962e3d9af45ff032ef168958cb1944a23084b2e13dd205829dd26e3df8a186e2b119bd920f5327f9efe5"
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