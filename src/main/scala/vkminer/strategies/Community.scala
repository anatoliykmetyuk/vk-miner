package vkminer.strategies

import scala.language.implicitConversions

import vkminer.dom.VkEnvironment

import org.json4s._
import org.json4s.native.JsonMethods._

import generic._

trait Community extends FriendsExpansion {import e._

  def apply(groupId: String, wall: Boolean = true): Graph = {
    val initialUsers: Graph = groups.getMembers(groupId).foldLeft(Graph.Nil) {(g, u) =>
      g ++ (User(u) ->: Location(u))
    }

    println(s"Initial graph size: ${initialUsers.nodes.size}")
    name {friendsLoop(
      graph = initialUsers
    , truncateOutskirts = true
    , depth = 0
    , wall  = wall)}
  }

}