package vkminer
package dom

import scala.language.implicitConversions

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait UserComponent {this: VkEnvironment =>
  val userFields = "sex,bdate,city,country,home_town,education,universities,schools,occupation,relation"

  val USER_PREFIX = "us"

  object users {
    def get(ids: String*): JValue = api.method(
      "users.get"
    , Map(
        "user_ids" -> ids.mkString(",")
      , "fields"   -> userFields
      , "v"        -> apiVersion
      )
    )
  }

  object friends {
    def get(u: User): JValue = api.method(
      "friends.get"
    , Map(
        "user_id" -> u.id.drop(2)
      , "fields"  -> userFields
      , "v"       -> apiVersion
      )
    )
  }

  object groups {
    def getMembers(gid: String): Seq[JValue] = {
      def loop(accum: Seq[JValue], iteration: Int, batch: Int): Seq[JValue] = {
        val response = api.method(
          "groups.getMembers"
        , Map(
            "group_id" -> gid
          , "offset"   -> (iteration * batch).toString
          , "fields"   -> userFields
          , "v"        -> apiVersion
          )
        )

        val items = (response \ "response" \ "items").extract[Seq[JValue]]

        if (items.isEmpty) accum else loop(accum ++ items, iteration + 1, batch)
      }
      loop(Nil, 0, 1000)
    }
  }


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
  
    lazy val Nil = User("", "", "", -1, None, None, None)
  }

}
