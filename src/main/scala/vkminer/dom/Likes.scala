package vkminer
package dom

import scala.language.implicitConversions

import org.json4s._
import org.json4s.native.JsonMethods._

trait LikesComponent {this: VkEnvironment =>

  object likes {
    def getList(ownerId: String, itemId: String, tpe: String, filter: String, count: Int = 100, offset: Int = 0): JValue = api.method(
      "likes.getList"
    , Map(
        "owner_id"     -> ownerId
      , "item_id"      -> itemId
      , "type"         -> tpe
      , "filter"       -> filter
      , "count"        -> count.toString
      , "offset"       -> offset.toString
      , "v"            -> apiVersion
      , "friends_only" -> "0"
      )
    )
  }

}