package vkminer
package dom

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait VkEnvironment extends Extractors
                       with UserComponent
                       with LocationComponent
                       with GraphComponent
                       with VkDatabaseApi {
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
    def getById(ids: Seq[String], methodName: String, paramName: String): Map[String, String] =
      (api.method(s"database.$methodName", Map(paramName -> ids.mkString(","), "v" -> apiVersion)) \ "response")
        .extract[Seq[JValue]]
        .map {implicit j => extractJson("id").get -> extractJson("title").get}
        .toMap

    def getCitiesById   (ids: Seq[String]) = getById(ids, "getCitiesById"   , "city_ids"   )
    def getCountriesById(ids: Seq[String]) = getById(ids, "getCountriesById", "country_ids")
  }

}