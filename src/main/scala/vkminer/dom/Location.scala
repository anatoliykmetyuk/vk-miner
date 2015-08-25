package vkminer
package dom

import scala.language.implicitConversions

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait LocationComponent {this: VkEnvironment =>

  case class Location(name: String, tpe: String, id: String) extends GraphNode {
    def toXml =
      <location>
        <name>{name}</name>
        <type>{tpe}</type>
        <id>{id}</id>
      </location>
  }

  object Location {
    def apply(implicit node: Node): Location = Location(
      name = extractXml("name").get
    , tpe  = extractXml("type").get
    , id   = extractXml("id"  ).get
    )

    /** User object JSON is supposed to be passed to this method. */
    def apply(json: JValue): Seq[Location] = {
      def nonEmptyLocation(l: Location): Boolean = l.id.drop(2) != "0"

      val city = (json \ "city").toOption.map {implicit j => Location(
        name = extractJson("title").get
      , tpe  = "city"
      , id   = "ci" + extractJson("id").get
      )}

      val country = (json \ "country").toOption.map {implicit j => Location(
        name = extractJson("title").get
      , tpe  = "country"
      , id   = "co" + extractJson("id").get
      )}

      val university = (json \ "university").toOption.map {_ => Location(
        name = extractJson("university_name")(json).get
      , tpe  = "university"
      , id   = "un" + extractJson("university")(json).get
      )}.flatMap {u => if (nonEmptyLocation(u)) Some(u) else None}

      val universities = (json \ "universities").extract[Seq[JValue]].map {implicit uni => Location(
        name = extractJson("name").get
      , tpe  = "university"
      , id   = "un" + extractJson("id").get
      )}.filter(nonEmptyLocation)

      val schools = (json \ "schools").extract[Seq[JValue]].map {implicit school => Location(
        name = extractJson("name").get
      , tpe  = "school"
      , id   = "sc" + extractJson("id").get
      )}

      implicit def optToSeq[T](opt: Option[T]): Seq[T] = opt.map(Seq(_)).getOrElse(Nil)
      
      (universities ++ schools ++ city ++ country ++ university).distinct
    }
  }

}
