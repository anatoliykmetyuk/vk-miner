package vkminer.strategies
package generic

import scala.language.implicitConversions

import vkminer.dom.VkEnvironment
import vkminer.util._

import org.json4s._
import org.json4s.native.JsonMethods._


trait FriendsExpansion extends BasicStrategy with ProgressBar with Wall {import e._

  def friendsLoop(
    previous         : Set[GraphNode] = Set()
  , graph            : Graph
  , iteration        : Int = 0
  , truncateOutskirts: Boolean = true
  , depth            : Int = 1
  , wall             : Boolean = true
  ): Graph = if (iteration <= depth) {
      println(s"Iteration $iteration of $depth. Database size: ${graph.nodes.size}. Edges: ${graph.edges.size}.")

      // Obtain the new users friends of whom need to be esteblished and connected
      val newUsers = graph.nodes.diff(previous).collect {case u: User => u}

      // For each user, we'll obtain his all friends (with their locations),
      // and connect them to this user. The results will be aggreated to this graph.
      val iterationGraph = newUsers.toList.zipWithIndex.foldLeft(Graph()) {case (g, (user, i)) =>
        progressBar(i, newUsers.size, "Progress")
        if (wall) println()

        val friendsGraph: Graph = {
          def task = user -->:[User] (friends.get(user) \ "response" \ "items").extract[Seq[JValue]]
            .foldLeft(Graph()) {(g, fj) => g ++ (User(fj) ->: Location(fj))}

          if (wall) withProgressBar(0, 5, "User") {task} else task
        }

        if (wall) {
          val visitorsGraph: Graph = wallVisitors(user.id.drop(2)).foldLeft(Graph.Nil) {case (g, (uid, weight)) =>
            val visitor = User.Nil.copy(id = USER_PREFIX + uid)
            val edge    = Edge.undirected(user.id, visitor.id, weight)
            g ++ Graph(Set(visitor), Set(edge))
          }
          print("\033[1A")

          // Call to VK API's "friends.get" method - get all the user's friends.
          // For each returned user, parse himself and his locations. Connect everything
          // into one graph.
          // Then, connect the `user` to every user in the resulting graph.
          // Finally, add the resulting graph into the acummulator `g`.
          // Yes, all these -->: and ->: methods are totally perverted and hard to read. But the
          // code became short and pretty! ^_^
          g +!+ friendsGraph +!+ visitorsGraph
        } else g ++ friendsGraph
      }

      // Finalize progress bar
      progressBar(newUsers.size, newUsers.size, "Progress")
      if (wall) print("\033[4B\n\r") else println()

      friendsLoop(
        previous  = graph.nodes
      , graph     = graph ++ iterationGraph
      , iteration = iteration + 1
      , truncateOutskirts = truncateOutskirts
      , depth             = depth
      , wall              = wall
      )
    }
    else if (!truncateOutskirts) graph
    else Graph(previous, graph.edges)


  def name(graph: Graph) = {
    println("Naming unnamed users")
    val withUserNames = nameUsers(graph).sanitize

    println("Naming the graph")
    val withLocationNames = nameGraph(withUserNames).sanitize
  
    println("Sanitizing location edges")
    locationEdgesToOne(withLocationNames).sanitize    
  }


  /* === Helpers for naming === */

  def nameUsers(graph: Graph): Graph = {
    val unnamedIds: Set[String] = graph.nodes.collect {case u: User if u.firstName.isEmpty && u.lastName.isEmpty => u.id}
    val namedUsers: Graph = (users.get(unnamedIds.map(_.drop(2)).toSeq: _*) \ "response").extract[Seq[JValue]].foldLeft(Graph.Nil) {(g, uj) =>
      g ++ (User(uj) ->: Location(uj))
    }

    graph.copy(nodes = graph.nodes.filter {n: GraphNode => !unnamedIds(n.id)}) ++ namedUsers
  }

  def locationEdgesToOne(graph: Graph): Graph = graph.copy(edges = graph.edges.map {
    case Edge(s, d, _) if s.isLocation || d.isLocation => Edge(s, d, 1)
    case x => x  
  })

  /** Finds location nodes without names and tries to obtain their names by ids. */
  def nameGraph(graph: Graph): Graph = {
    val locations: Set[Location] = graph.nodes.collect {case l: Location => l}

    val unnamedCities    = locations.filter {case Location(name, "city"   , _) if name.isEmpty => true case _ => false}
    val unnamedCountries = locations.filter {case Location(name, "country", _) if name.isEmpty => true case _ => false}
  
    val cityIds    = unnamedCities   .map(_.id.drop(2))
    val countryIds = unnamedCountries.map(_.id.drop(2))

    val countries = database.getCountriesById(countryIds.toSeq)
    val cities    = database.getCitiesById   (cityIds   .toSeq)

    def prefixed(m: Map[String, String], prefix: String): Map[String, String] =
      m.map {case (k, v) => s"$prefix$k" -> v}

    val names = prefixed(countries, "co") ++ prefixed(cities, "ci")

    graph.copy(nodes = graph.nodes.map {
      case l @ Location(name, tpe, id) if name.isEmpty => l.copy(name = names(id))
      case x => x
    })
  }
}
