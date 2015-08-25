package vkminer.strategies

import vkminer.dom.VkEnvironment

import org.json4s._
import org.json4s.native.JsonMethods._


class FullEgoGroup[E <: VkEnvironment](val e: E) {
  import e._

  def apply(id: String, depth: Int = 1, truncateOutskirts: Boolean = true): Graph = {
    val initialUserJson      = (users.get(id) \ "response")(0)

    val initialUser          = User    (initialUserJson)
    val initialUserLocations = Location(initialUserJson)
    val initialEdges         = Edge.uOneToMany(initialUser, initialUserLocations)

    println(s"Initial user: $initialUser; Locations: $initialUserLocations")

    def loop(previous: Set[GraphNode], current: Set[GraphNode], edges: Set[Edge], iteration: Int): Graph = if (iteration <= depth) {
      println(s"Iteration $iteration of $depth. Database size: ${current.size}. Edges: ${edges.size}.")

      val newUsers: Set[User] = (current diff previous).filter(_.isInstanceOf[User]).map(_.asInstanceOf[User])
      val yNodes = new collection.mutable.SetBuilder[GraphNode, Set[GraphNode]](Set())
      val yEdges = new collection.mutable.SetBuilder[Edge     , Set[Edge     ]](Set())

      for ((u, i) <- newUsers.toList.zipWithIndex) {
        progressBar(i, newUsers.size)

        val rawFriends: Seq[JValue] = (friends.get(u) \ "response" \ "items").extract[Seq[JValue]]

        val packedFriends: Seq[(User, Seq[Location], Seq[Edge])] = rawFriends.map {fj =>
          try {
            val friend    = User    (fj)
            val locations = Location(fj)
            val edges     = Edge.uOneToMany(friend, locations)
            (friend, locations, edges)
          } catch {case t: Throwable =>
            println(s"Exception while processing $fj")
            throw t
          }
        }

        val uFriends               = packedFriends.map    (_._1)
        val locations              = packedFriends.flatMap(_._2)
        val friends2locationsEdges = packedFriends.flatMap(_._3)


        val edges: Seq[Edge] = Edge.uOneToMany(u, uFriends)

        yNodes ++= uFriends ++ locations
        yEdges ++= edges ++ friends2locationsEdges
      }
      println()
      loop(current, current ++ yNodes.result(), edges ++ yEdges.result(), iteration + 1)
    }
    else if (!truncateOutskirts) Graph(current.toList, edges.toList)
    else {
      val newNodes      = current.diff(previous).map(_.id)
      val filteredEdges = edges.filter {e => !newNodes.contains(e.sourceId) && !newNodes.contains(e.targetId)}
      Graph(previous.toList, filteredEdges.toList)
    }
    
    loop(Set(), Set(initialUser) ++ initialUserLocations, initialEdges.toSet, 0)
  }

  def progressBar(i: Int, max: Int, size: Int = 100) {
    val ratio  = i.toDouble / max
    val filled = (size * ratio).toInt
    val empty  = (size - filled).toInt

    val bar = s"[${"#" * filled}${" " * empty}] ${(ratio * 100).toInt}% $i/$max"
    print(s"\r$bar")
  }
}