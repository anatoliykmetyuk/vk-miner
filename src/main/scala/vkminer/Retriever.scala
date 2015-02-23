package vkminer

import java.io.File

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import org.apache.commons.io._


class Retriever(val api: VkApi, val subdir: String) {
  import api._
  
  val wd         = s"/Users/anatolii/Projects/vk-mining/data/$subdir"
  val usersFile  = "users.json"
  val groupsFile = "groups.json"

  implicit val formats = org.json4s.DefaultFormats

  def users(params: Map[String, String] = Map(), append: Boolean = false) {
    val defaultParams = Map(
      "count"   -> "10",
      "city"    -> "292",
      "country" -> "2",
      "sex"     -> "1",
      "age_to"  -> "25"
    )

    val users = method("users.search", defaultParams ++ params)

    val refined = users.transformField {case ("response", JArray(ets)) => "response" -> JArray(ets.drop(1))} \ "response"

    val serialized = pretty(render(refined))
    
    FileUtils.writeStringToFile(new File(wd, usersFile), serialized, append)
  }

  def addGroups(params: Map[String, String] = Map()) {
    def defaultParams(id: String) = Map(
      "user_id"  -> id,
      "extended" -> "1"
    )

    val usersString = FileUtils.readFileToString(new File(wd, usersFile))
    val usersJson   = parse(usersString)

    val usersWithIndexes = usersJson
      .extract[List[JObject]]
      .zipWithIndex
      .map {case (u, idx) => (u, idx + 1)}

    val total = usersWithIndexes.size

    val usersWithGroups = usersWithIndexes.map {case (obj, idx) =>
      val id = (obj \ "uid").extract[String]
      println(s"Refining $idx of $total")
      obj ~ ("groups" -> groupsOfUser(defaultParams(id) ++ params))
    }

    val result = JArray(usersWithGroups)

    val serialized = pretty(render(result))

    FileUtils.writeStringToFile(new File(wd, groupsFile), serialized)
  }

  def groupsOfUser(params: Map[String, String]): JArray = JArray {
    (method("groups.get", params) \ "response").extract[List[JValue]].drop(1)
  }

}