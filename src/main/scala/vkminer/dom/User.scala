package vkminer
package dom

import scala.language.implicitConversions

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait UserComponent {this: VkEnvironment =>

  case class User(
    id       : String
  , firstName: String
  , lastName : String

  , sex       : Int
  , birthday  : Option[String]
  , occupation: Option[String]
  , homeTown  : Option[String]
  ) extends GraphNode {

    def age: Option[Int] = {
      val bdayPat = """(\d+).(\d+).(\d+)""".r
      birthday.flatMap {
        case bdayPat(d, m, y) =>
          import java.util.Calendar
          import Calendar._

          val now  = Calendar.getInstance
          val bday = Calendar.getInstance
          bday.set(y.toInt, m.toInt - 1, d.toInt)

          val diff = now.getTimeInMillis - bday.getTimeInMillis
          Some(diff / 1000 / 60 / 60 / 24 / 365).map(_.toInt)

        case _ => None
      }
    }

    def toXml =
      <user>
        <id>{id}</id>
        <firstName>{firstName}</firstName>
        <lastName>{lastName}</lastName>
   
        <sex>{sex}</sex>
        {birthday  .map {b => <birthday>{b}</birthday>    }.getOrElse(())}
        {occupation.map {o => <occupation>{o}</occupation>}.getOrElse(())}
        {homeTown  .map {h => <homeTown>{h}</homeTown>    }.getOrElse(())}
      </user>
  }

  object User {
    def apply(implicit node: Node): User = User(
      id        = extractXml("id"      ).get
    , firstName = extractXml("firstName").get
    , lastName  = extractXml("lastName" ).get

    , sex        = extractXml("sex"       ).get.toInt
    , birthday   = extractXml("birthday"  )
    , occupation = extractXml("occupation")
    , homeTown   = extractXml("homeTown"  )
    )

    def apply(implicit json: JValue): User = User(
      id        = "us" + extractJson("id" ).get
    , firstName = extractJson("first_name").get
    , lastName  = extractJson("last_name" ).get

    , sex        = extractJson("sex").get.toInt
    , birthday   = extractJson("bdate")
    , occupation = extractJson("type")(json \ "occupation")
    , homeTown   = extractJson("home_town")
    )
  }

}
