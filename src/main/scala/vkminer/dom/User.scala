package vkminer
package dom

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._


trait UserComponent {this: VkEnvironment =>

  case class User(uid: Long, firstName: String, lastName: String) {
    def toXml =
      <user>
        <uid>{uid}</uid>
        <firstName>{firstName}</firstName>
        <lastName>{lastName}</lastName>
      </user>
  }

  object User {
    def apply(node: Node): User = User(
      uid       = (node \ "uid").text.toLong
    , firstName = (node \ "firstName").text
    , lastName  = (node \ "lastName" ).text
    )

    def apply(json: JValue): User = User(
      uid       = (json \ "uid"       ).extract[Long  ]
    , firstName = (json \ "first_name").extract[String]
    , lastName  = (json \ "last_name" ).extract[String]
    )
  }
}

trait EdgeComponent {
  class Edge(_uid1: Long, _uid2: Long) {
    val uid1 = math.min(_uid1, _uid2)
    val uid2 = math.max(_uid1, _uid2)

    override def equals(that: Any): Boolean = that match {
      case e: Edge => uid1 == e.uid1 && uid2 == e.uid2
      case _ => false
    }

    override def hashCode = (uid1 + uid2).toInt

    override def toString = s"Edge($uid1, $uid2)"

    def toXml =
      <edge>
        <uid1>{uid1}</uid1>
        <uid2>{uid2}</uid2>
      </edge>
  }

  object Edge {
    def apply(uid1: Long, uid2: Long): Edge = new Edge(uid1, uid2)
    def apply(node: Node): Edge = Edge(
      uid1 = (node \ "uid1").text.toLong
    , uid2 = (node \ "uid2").text.toLong
    )
  }
}

