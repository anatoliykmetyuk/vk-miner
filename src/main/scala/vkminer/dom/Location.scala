package vkminer
package dom

import scala.language.implicitConversions

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait LocationComponent {this: VkEnvironment =>

  val universities: Map[String, (String, String)]

  val LOCTYPE_CITY        = "city"
  val LOCTYPE_CITY_PREFIX = "ci"

  val LOCTYPE_COUNTRY        = "country"
  val LOCTYPE_COUNTRY_PREFIX = "co"

  val LOCTYPE_UNIVERSITY        = "university"
  val LOCTYPE_UNIVERSITY_PREFIX = "un"

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
    def apply(json: JValue): Graph = {
      def nonEmptyLocation(l: Location): Boolean = l.id.drop(2) != "0"

      // Current locations
      val city: Option[Location] = (json \ "city").toOption.map {implicit j => Location(
        name = extractJson("title").get
      , tpe  = "city"
      , id   = "ci" + extractJson("id").get
      )}

      val country: Option[Location] = (json \ "country").toOption.map {implicit j => Location(
        name = extractJson("title").get
      , tpe  = "country"
      , id   = "co" + extractJson("id").get
      )}

      val currentLocation: Graph = {
        val nodes = Set(city, country).collect {case Some(l) => l}
        val edges = Edge.uAll(nodes.toSeq)
        Graph(nodes.asInstanceOf[Set[GraphNode]], edges.toSet)
      }

      // Current university
      val university: Option[Location] = (json \ "university").toOption.map {_ => Location(
        name = extractJson("university_name")(json).get
      , tpe  = "university"
      , id   = "un" + extractJson("university")(json).get
      )}

      def universityGraph(u: Location): Graph = universities.get(u.id.drop(2)).map {case (coId, ciId) =>
        val city    = Location("", LOCTYPE_CITY   , LOCTYPE_CITY_PREFIX    + ciId)
        val country = Location("", LOCTYPE_COUNTRY, LOCTYPE_COUNTRY_PREFIX + coId)
        u ->: city ->: country ->: Graph.Nil
      }.getOrElse(u ->: Graph.Nil)

      val universityWithLocation: Graph = university.map {universityGraph}.getOrElse(Graph.Nil)


      // Helper extractors for archived locations
      def extractCountry(implicit json: JValue) = Location(
        id   = "co" + extractJson("country").get
      , tpe  = "country"
      , name = ""
      )

      def extractCity(implicit json: JValue) = Location(
        id   = "ci" + extractJson("city").get
      , tpe  = "city"
      , name = ""
      )


      // Archived locations
      val universitiesGraph: Graph = (json \ "universities").extract[Seq[JValue]].foldLeft(Graph()) {(g, u) =>
        implicit val uni = u

        val university = Location(
          name = extractJson("name").get
        , tpe  = LOCTYPE_UNIVERSITY
        , id   = LOCTYPE_UNIVERSITY_PREFIX + extractJson("id").get
        )

        g ++ universityGraph(university)
      }

      val schools = (json \ "schools").extract[Seq[JValue]].foldLeft(Graph()) {(g, s) =>
        implicit val sc = s

        val school = Location(
          name = extractJson("name").get
        , tpe  = "school"
        , id   = "sc" + extractJson("id").get
        )
        val country = extractCountry
        val city    = extractCity

        g ++ (school ->: country ->: city ->: Graph.Nil)
      }

      (currentLocation ++ universityWithLocation ++ universitiesGraph ++ schools).sanitize
    }
  }

}
