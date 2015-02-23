package vkminer.analyzer

import java.io.File

import org.apache.commons.io._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import vkminer._

class Analyzer(token: String, experimentName: String) {
  val api       = new VkApi(token)
  val retriever = new Retriever(api, experimentName)

  implicit val formats = org.json4s.DefaultFormats

  lazy val model: Seq[User] = {
    val rawJson = FileUtils.readFileToString(new File(retriever.wd, retriever.groupsFile))
    val json = parse(rawJson).transformField {
      // User
      case ("uid"       , x) => "id"        -> x
      case ("first_name", x) => "firstName" -> x
      case ("last_name" , x) => "lastName"  -> x

      // Group
      case ("gid"        , x) => "id"         -> x
      case ("screen_name", x) => "screenName" -> x
      case ("type"       , x) => "groupType"  -> x
    }

    json.extract[Seq[User]]
  }

  def getData(userParams: Map[String, String] = Map(), groupParams: Map[String, String] = Map()) {
    retriever.users(userParams)
    retriever.addGroups(groupParams)
  }

}