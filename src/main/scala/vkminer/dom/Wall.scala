package vkminer
package dom

import scala.language.implicitConversions

import org.json4s._
import org.json4s.native.JsonMethods._

trait WallComponent {this: VkEnvironment =>

  object wall {
    def get(ownerId: String, filter: String = "all", count: Int = 100, offset: Int = 0): JValue = api.method(
      "wall.get"
    , Map(
        "owner_id" -> ownerId
      , "filter"   -> filter
      , "offset"   -> offset.toString
      , "count"    -> count.toString
      , "v"        -> apiVersion
      )
    )

    def getComments(wallOwnerId: String, postId: String, count: Int = 100, offset: Int = 0): JValue = api.method(
      "wall.getComments"
    , Map(
        "owner_id"   -> wallOwnerId
      , "post_id"    -> postId
      , "count"      -> count.toString
      , "offset"     -> offset.toString
      , "need_likes" -> "1"
      , "v"          -> apiVersion
      )
    )
  }

}