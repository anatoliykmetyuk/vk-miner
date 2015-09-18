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
    |val token = "4db5262fb5b749ba12d1e83f02d49b0ab162d2feec7d472eb70d16824b623cba724a6442e084dc4b7898c"
    |// val gb = new GraphBuilder(api)
    |
    |val env = new VkEnvironment with XmlSerializerComponent
    |                          with GexfSerializerComponent
    |                          with UniversitiesSerializerComponent {
    |  val workingDirectory = "/Users/anatolii/Desktop"
    |  val api = new VkApi(token)
    |  val universities = UniversitiesSerializer.deserialize("universities")
    |}
    |import env._
    |val api = new VkApi(token)
    |
    |val ego = new FullEgoGroup {
    |  override type E   = env.type     
    |  override val e: E = env
    |}
    |
    |val com = new Community {
    |  override type E   = env.type
    |  override val e: E = env    
    |}
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