package vkminer.strategies

import vkminer.dom.VkEnvironment

import org.json4s._
import org.json4s.native.JsonMethods._


class FullEgoGroup[E <: VkEnvironment](val e: E) {
  import e._

  def apply(id: String, depth: Int = 1, truncateOutskirts: Boolean = true): Graph = {
    val initialUserJson  = (users.get(id) \ "response")(0)
    val initialUserGraph = User(initialUserJson) ->: Location(initialUserJson)

    println(s"Initial graph: $initialUserGraph")

    def loop(previous: Set[GraphNode], graph: Graph, iteration: Int): Graph = if (iteration <= depth) {
      println(s"Iteration $iteration of $depth. Database size: ${graph.nodes.size}. Edges: ${graph.edges.size}.")

      // Obtain the new users friends of whom need to be esteblished and connected
      val newUsers = (graph.nodes diff previous).filter(_.isInstanceOf[User]).map(_.asInstanceOf[User])

      // For each user, we'll obtain his all friends (with their locations),
      // and connect them to this user. The results will be aggreated to this graph.
      val iterationGraph = newUsers.toList.zipWithIndex.foldLeft(Graph()) {case (g, (user, i)) =>
        progressBar(i, newUsers.size)

        // Call to VK API's "friends.get" method - get all the user's friends.
        // For each returned user, parse himself and his locations. Connect everything
        // into one graph.
        // Then, connect the `user` to every user in the resulting graph.
        // Finally, add the resulting graph into the acummulator `g`.
        // Yes, all these -->: and ->: methods are totally perverted and hard to read. But the
        // code became short and pretty! ^_^
        g ++ (user -->:[User] (friends.get(user) \ "response" \ "items").extract[Seq[JValue]]
          .foldLeft(Graph()) {(g, fj) => g ++ (User(fj) ->: Location(fj))})
      }

      // Finalize progress bar
      progressBar(newUsers.size, newUsers.size); println()

      loop(graph.nodes, graph ++ iterationGraph, iteration + 1)
    }
    else if (!truncateOutskirts) graph
    else Graph(previous, graph.edges)
    
    val raw = loop(Set(), initialUserGraph, 0).sanitize

    println("Naming the graph")
    nameGraph(raw)
  }

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
      case l @ Location(name, tpe, id) if name.isEmpty => l.copy(name = names(name))
      case x => x
    })
  }

  def progressBar(i: Int, max: Int, size: Int = 100) {
    val ratio  = i.toDouble / max
    val filled = (size * ratio).toInt
    val empty  = (size - filled).toInt

    val bar = s"[${"#" * filled}${" " * empty}] ${(ratio * 100).toInt}% $i/$max"
    print(s"\r$bar")
  }
}