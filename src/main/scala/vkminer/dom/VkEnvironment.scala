package vkminer
package dom

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait VkEnvironment extends Extractors
                       with UserComponent
                       with LocationComponent
                       with GraphComponent
                       with VkDatabaseApi
                       with WallComponent
                       with LikesComponent {
  val api: VkApi
  val apiVersion = "5.37"

  implicit val jsonFormats = org.json4s.DefaultFormats
}

trait Extractors {this: VkEnvironment =>
  def extractXml (id: String)(implicit node: Node  ): Option[String] = (node \ id).headOption.map(_.text)
  def extractJson(id: String)(implicit node: JValue): Option[String] = (node \ id).toOption.map(_.values.toString)
}

trait VkDatabaseApi {this: VkEnvironment =>

  object database {

    def id2titleMap(method: String, params: Map[String, String]): Map[String, String] =
      (api.method(s"database.$method", params + ("v" -> apiVersion)) \ "response" \ "items")
        .extract[Seq[JValue]]
        .map {implicit j => extractJson("id").get -> extractJson("title").get}
        .toMap


    def getById(ids: Seq[String], method: String, paramName: String): Map[String, String] =
      (api.method(s"database.$method", Map(paramName -> ids.mkString(","), "v" -> apiVersion)) \ "response")
        .extract[Seq[JValue]]
        .map {implicit j => extractJson("id").get -> extractJson("title").get}
        .toMap

    def getCitiesById   (ids: Seq[String]) = getById(ids, "getCitiesById"   , "city_ids"   )
    def getCountriesById(ids: Seq[String]) = getById(ids, "getCountriesById", "country_ids")
  
    def getCountries(needAll: Boolean, count: Int = 10, offset: Int = 0): Map[String, String] =
      id2titleMap("getCountries", Map(
        "need_all" -> {if (needAll) "1" else "0"}
      , "offset"   -> offset.toString
      , "count"    -> count.toString
      ))

    def getCities(countryId: Int, needAll: Boolean, count: Int = 100, offset: Int = 0): Map[String, String] =
      id2titleMap("getCities", Map(
        "country_id" -> countryId.toString
      , "needAll"    -> {if (needAll) "1" else "0"}
      , "count"      -> count.toString
      , "offset"     -> offset.toString
      , "v"          -> apiVersion
      ))

    def getUniversities(cityId: Int, count: Int = 1000, offset: Int = 0): Map[String, String] =
      id2titleMap("getUniversities", Map(
        "city_id" -> cityId.toString
      , "count"   -> count .toString
      , "offset"  -> offset.toString
      ))
  }


}