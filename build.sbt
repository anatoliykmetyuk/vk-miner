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
  |implicit val formats = org.json4s.DefaultFormats
  |
  |val token = "167a139a663b46cee17b96828bf71d9ed57975e4e576546237560cf9c235a4c921b892366ccbbbd95ece2"
  |
  |def analyzer(name: String) = new Analyzer(token, name)
""".stripMargin

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4",
  "org.apache.httpcomponents" % "httpclient" % "4.4",
  "org.json4s" %% "json4s-native" % "3.2.11"
)