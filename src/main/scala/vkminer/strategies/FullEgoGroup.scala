package vkminer.strategies

import scala.language.implicitConversions

import vkminer.dom.VkEnvironment

import org.json4s._
import org.json4s.native.JsonMethods._

import generic._

trait FullEgoGroup extends FriendsExpansion {import e._

  def apply(id: String, depth: Int = 1, truncateOutskirts: Boolean = true): Graph = {
    val initialUserJson  = (users.get(id) \ "response")(0)
    val initialUserGraph = User(initialUserJson) ->: Location(initialUserJson)

    println(s"Initial graph: $initialUserGraph")
    name {friendsLoop(graph = initialUserGraph, depth = depth, truncateOutskirts = truncateOutskirts)}
  }

}