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

      val currentLocation: Option[Graph] = city.map {c =>
        Graph(Set(c, country.get), Set(Edge.undirected(c.id, country.get.id)))
      }

      val university: Option[Graph] = (json \ "university").toOption.map {_ => Location(
        name = extractJson("university_name")(json).get
      , tpe  = "university"
      , id   = "un" + extractJson("university")(json).get
      )}.map {u => Graph(nodes = Set(u))}


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
      val universities: Graph = (json \ "universities").extract[Seq[JValue]].foldLeft(Graph()) {(g, u) =>
        implicit val uni = u

        val university = Location(
          name = extractJson("name").get
        , tpe  = "university"
        , id   = "un" + extractJson("id").get
        )
        val country = extractCountry
        val city    = extractCity

        val locations = Seq(university, city, country)
        val edges     = Edge.uAll(locations)
        Graph(g.nodes ++ locations, g.edges ++ edges)
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

        val locations = Seq(school, country, city)
        val edges     = Edge.uAll(locations)
        Graph(g.nodes ++ locations, g.edges ++ edges)
      }


      // Result
      (Seq(currentLocation, university).collect {case Some(g) => g}.reduce {_ ++ _} ++ universities ++ schools).sanitize
    }
  }

}
