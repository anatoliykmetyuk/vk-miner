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

