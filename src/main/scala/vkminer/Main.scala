package vkminer

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

import org.apache.commons.io._

import java.io.File

object Main {

  val WORKING_DIR = "/Users/anatolii/Desktop/mining"

  def serialize(graph: Graph, file: String, dir: String = WORKING_DIR) {
    val pp = new PrettyPrinter(200, 2)
    val xml = pp format graph.toXml
    FileUtils.writeStringToFile(new File(dir, file), xml)
  }

  def deserialize(file: String, dir: String = WORKING_DIR): Graph = {
    val xml = XML loadFile new File(dir, file)
    Graph(xml)
  }


  def toGml(graph: Graph, file: String, dir: String = WORKING_DIR) {
    val users = graph.users.map {u =>
      s"""node
      |[
      | id ${u.uid}
      | label "${u.firstName} ${u.lastName}"
      |]""".stripMargin
    }.mkString("\n")

    val edges = graph.edges.map {e =>
      s"""edge
      |[
      | source ${e.uid1}
      | target ${e.uid2}
      |]""".stripMargin
    }.mkString("\n")

    val graphStr = s"""graph
    |[
    |$users
    |$edges
    |]""".stripMargin

    FileUtils.writeStringToFile(new File(dir, file), graphStr)
  }

}

class GraphBuilder(api: VkApi) {
  implicit val formats = org.json4s.DefaultFormats

  def progressBar(i: Int, max: Int, size: Int = 100) {
    val ratio  = i.toDouble / max
    val filled = (size * ratio).toInt
    val empty  = (size - filled).toInt

    val bar = s"[${"#" * filled}${" " * empty}] ${(ratio * 100).toInt}% $i/$max"
    print(s"\r$bar")
  }

  def expandFrom(uid: Long, depth: Int = 1, truncateOutskirts: Boolean = true): Graph = {
    val initialUserJson = (api.method("users.get", Map("user_ids" -> uid.toString)) \ "response")(0)
    val initialUser = User(initialUserJson)
    println(s"Initial user: $initialUser")

    def loop(previous: Set[User], current: Set[User], edges: Set[Edge], iteration: Int): Graph = if (iteration <= depth) {
      println(s"Iteration $iteration of $depth. Database size: ${current.size}. Edges: ${edges.size}.")
      
      val newUsers = current diff previous
      val yUsers = new collection.mutable.SetBuilder[User, Set[User]](Set())
      val yEdges = new collection.mutable.SetBuilder[Edge, Set[Edge]](Set())

      for ((u, i) <- newUsers.toList.zipWithIndex) {
        progressBar(i, newUsers.size)
        val friends: Seq[User] = (api.method("friends.get", Map("user_id" -> u.uid.toString, "fields" -> "1")) \ "response").extract[Seq[JValue]].map(User(_))
        val edges  : Seq[Edge] = friends.map {f => Edge(u.uid, f.uid)}

        yUsers ++= friends
        yEdges ++= edges
      }
      println()
      loop(current, current ++ yUsers.result(), edges ++ yEdges.result(), iteration + 1)
    } else if (!truncateOutskirts) Graph(current.toList, edges.toList)
    else {
      val newUsers = current.diff(previous).map(_.uid)
      val filteredEdges = edges.filter {e => !newUsers.contains(e.uid1) && !newUsers.contains(e.uid2)}
      Graph(previous.toList, filteredEdges.toList)
    }
    
    loop(Set(), Set(initialUser), Set(), 0)
  }
}


case class Graph(users: Seq[User], edges: Seq[Edge]) {
  def toXml: Node =
<network>
  <users>
    {for (u <- users) yield u.toXml}
  </users>
  <edges>
    {for (e <- edges) yield e.toXml}
  </edges>
</network> 
}

object Graph {
  def apply(node: Node): Graph = {
    val users = (node \ "users" \ "_").map(User(_))
    val edges = (node \ "edges" \ "_").map(Edge(_))
    Graph(users, edges)
  }
}

case class User(uid: Long, firstName: String, lastName: String) {
  def toXml =
<user>
  <uid>{uid}</uid>
  <firstName>{firstName}</firstName>
  <lastName>{lastName}</lastName>
</user>
}

object User {
  implicit val formats = org.json4s.DefaultFormats

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