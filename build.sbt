sbtVersion   := "0.13.7"
scalaVersion := "2.11.5"

initialCommands := """
  |import vkminer._
  |import vkminer.analyzer._
  |
  |import org.json4s._
  |import org.json4s.native.JsonMethods._
  |import org.json4s.JsonDSL._
  |
  |import org.apache.spark.SparkContext
  |import org.apache.spark.SparkContext._
  |import org.apache.spark.SparkConf
  |
  |import StringMethods._
  |
  |implicit val formats = org.json4s.DefaultFormats
  |
  |val token = "6408ab38a3280dc6f0f3ff7678db41ce4370dbc8ab359a18911d340eb7255250281dc627d53c9eca066fe"
  |val academyGroupId = "59832613"
  |
  |def analyzer(name: String) = new Analyzer(token, name)
  |
  |val sc = new SparkContext(new SparkConf().setAppName("I own it!").setMaster("local[2]"))
  |val clusterization = new Clusterization(sc)
  |import clusterization._
""".stripMargin

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4",
  "org.apache.httpcomponents" % "httpclient" % "4.4",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.apache.spark" %% "spark-mllib" % "1.2.1"
)