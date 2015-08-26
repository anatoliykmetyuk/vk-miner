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
      val newUsers = graph.nodes.diff(previous).collect {case u: User => u}

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
          .foldLeft(Graph()) {(g, fj) =>
            try g ++ (User(fj) ->: Location(fj))
            catch {case t: Throwable => println(pretty(render(fj))); throw t}
          })
      }

      // Finalize progress bar
      progressBar(newUsers.size, newUsers.size); println()

      loop(graph.nodes, graph ++ iterationGraph, iteration + 1)
    }
    else if (!truncateOutskirts) graph
    else Graph(previous, graph.edges)
    
    val raw = loop(Set(), initialUserGraph, 0).sanitize

    println("Naming the graph")
    nameGraph(raw).sanitize
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
      case l @ Location(name, tpe, id) if name.isEmpty => l.copy(name = names(id))
      case x => x
    })
  }

  def progressBar(i: Int, max: Int, label: String = "", size: Int = 100) {
    val ratio  = i.toDouble / max
    val filled = (size * ratio).toInt
    val empty  = (size - filled).toInt

    val bar = s"$label: [${"#" * filled}${" " * empty}] ${(ratio * 100).toInt}% $i/$max"
    print(s"\r$bar")
  }

  def withProgressBar[T](i: Int, max: Int, label: String = "")(task: => T): T = {
    progressBar(i, max, label)
    val res = task
    progressBar(i + 1, max, label)
    res
  }


  trait WallEntity {
    val postId: String
    val fromId: String
    val likes : Int
  }
  case class Post   (postId: String, fromId: String, comments: Int, likes: Int, shares: Int) extends WallEntity
  case class Comment(postId: String, fromId: String, likes: Int                            ) extends WallEntity

  def wallOf(id: String, count: Int = 20, printProgress: Boolean = false): Seq[Post] = {
    def postsOf(filter: String, idx: Int): Seq[Post] = {
      def task = (wall.get(id, filter, count / 2) \ "response" \ "items").extract[Seq[JValue]].map {implicit j =>
        Post(extractJson("id").get, extractJson("from_id").get,
          extractJson("count")(j \ "comments").get.toInt, extractJson("count")(j \ "likes").get.toInt,
          extractJson("count")(j \ "reposts").get.toInt)
      }

      if (printProgress) withProgressBar(idx, 2, "Wall posts")(task)
      else task
    }

    postsOf("owner", 0) ++ postsOf("others", 1)
  }

  def commentsOf(wallOwnerId: String, posts: Seq[Post], printProgress: Boolean = false): Seq[Comment] = {
    val haveComments = posts.filter(_.comments > 0)

    haveComments.zipWithIndex.flatMap {case (Post(postId, _, comments, _, _), idx) =>
      def task = (wall.getComments(wallOwnerId, postId, comments) \ "response" \ "items").extract[Seq[JValue]].map {implicit j =>
        Comment(extractJson("id").get, extractJson("from_id").get, extractJson("count")(j \ "likes").get.toInt)
      }

      if (printProgress) withProgressBar(idx, haveComments.size, "Comments")(task)
      else task
    }
  }


  def likesOrSharesOf(wallOwnerId: String, entities: Seq[WallEntity], likesFilter: Boolean, printProgress: Boolean = false): Seq[String] = {
    val target =
      if (likesFilter) entities.filter(_.likes  > 0)
      else       entities.collect {case p @ Post(_, _, _, _, shares) if shares > 0 => p}

    val filter = if (likesFilter) "likes" else "copies"
    val label  = if (likesFilter) "Likes" else "Shares"

    target.zipWithIndex.flatMap {case (e, idx) =>
      val tpe = e match {case _: Post => "post" case _: Comment => "comment"}
      def task = (likes.getList(wallOwnerId, e.postId, tpe, filter) \ "response" \ "items").extract[Seq[Long]].map(_.toString)
      
      if (printProgress) withProgressBar(idx, target.size, label)(task)
      else task
    }
  }

  /** People who like these. */
  def likesOf(wallOwnerId: String, entities: Seq[WallEntity], printProgress: Boolean = false): Seq[String] =
    likesOrSharesOf(wallOwnerId, entities, true, printProgress)
  
  /** People who shared these. */
  def sharesOf(wallOwnerId: String, entities: Seq[WallEntity], printProgress: Boolean = false): Seq[String] =
    likesOrSharesOf(wallOwnerId, entities, false, printProgress)


}