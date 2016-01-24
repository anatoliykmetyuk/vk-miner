package vkminer.strategies
package generic

import scala.language.implicitConversions

import vkminer.dom.VkEnvironment
import vkminer.util._

import org.json4s._
import org.json4s.native.JsonMethods._


trait Wall extends BasicStrategy with ProgressBar {import e._
  import FriendsExpansion._

  trait WallEntity {
    val postId: String
    val fromId: String
    val likes : Int
  }
  case class Post   (postId: String, fromId: String, comments: Int, likes: Int, shares: Int) extends WallEntity
  case class Comment(postId: String, fromId: String, likes: Int                            ) extends WallEntity

  def wallOf(id: String, count: Int = 20, printProgress: Boolean = true): Seq[Post] = {
    def postsOf(filter: String, idx: Int): Seq[Post] = {
      def task = (wall.get(id, filter, count / 2) \ "response" \ "items").extract[Seq[JValue]].map {implicit j =>
        Post(extractJson("id").get, extractJson("from_id").get,
          extractJson("count")(j \ "comments").get.toInt, extractJson("count")(j \ "likes").get.toInt,
          extractJson("count")(j \ "reposts").get.toInt)
      }

      if (printProgress) withProgressBar(idx, 2, USER, "Posts")(task)
      else task
    }

    postsOf("owner", 0) ++ postsOf("others", 1)
  }

  def commentsOf(wallOwnerId: String, posts: Seq[Post], printProgress: Boolean = true): Seq[Comment] = {
    val haveComments = posts.filter(_.comments > 0)

    haveComments.zipWithIndex.flatMap {case (Post(postId, _, comments, _, _), idx) =>
      def task = (wall.getComments(wallOwnerId, postId) \ "response" \ "items").extract[Seq[JValue]].map {implicit j =>
        Comment(extractJson("id").get, extractJson("from_id").get, extractJson("count")(j \ "likes").get.toInt)
      }

      if (printProgress) withProgressBar(idx, haveComments.size, USER, "Comments")(task)
      else task
    }
  }


  def likesOrSharesOf(wallOwnerId: String, entities: Seq[WallEntity], likesFilter: Boolean, printProgress: Boolean = true): Seq[String] = {
    val target =
      if (likesFilter) entities.filter(_.likes  > 0)
      else       entities.collect {case p @ Post(_, _, _, _, shares) if shares > 0 => p}

    val filter = if (likesFilter) "likes" else "copies"
    val label  = if (likesFilter) "Likes" else "Shares"

    target.zipWithIndex.flatMap {case (e, idx) =>
      val tpe = e match {case _: Post => "post" case _: Comment => "comment"}
      def task = (likes.getList(wallOwnerId, e.postId, tpe, filter) \ "response" \ "items").extract[Seq[Long]].map(_.toString)
      
      if (printProgress) withProgressBar(idx, target.size, USER, label)(task)
      else task
    }
  }

  /** People who like these. */
  def likesOf(wallOwnerId: String, entities: Seq[WallEntity], printProgress: Boolean = true): Seq[String] =
    likesOrSharesOf(wallOwnerId, entities, true, printProgress)
  
  /** People who shared these. */
  def sharesOf(wallOwnerId: String, entities: Seq[WallEntity], printProgress: Boolean = true): Seq[String] =
    likesOrSharesOf(wallOwnerId, entities, false, printProgress)


  def wallVisitors(wallOwnerId: String): Seq[(String, Double)] = {
    def pb[T](i: Int)(task: => T): T = withProgressBar[T](i, 5, USER, "User") {
      println()
      val result = task
      print("\033[1A")
      result
    }

    val posts    = pb(1) {wallOf    (wallOwnerId)}
    val comments = pb(2) {commentsOf(wallOwnerId, posts)}

    val entities: Seq[WallEntity] = posts ++ comments

    val uLikes  = pb(3) {likesOf (wallOwnerId, entities)}
    val uShares = pb(4) {sharesOf(wallOwnerId, entities)}

    val lowPriority : Seq[(String, Double)] = (uLikes ++ uShares)   .groupBy(x => x).toSeq.map {case (k, v) => k -> v.size / 10D   }
    val highPriority: Seq[(String, Double)] = entities.map(_.fromId).groupBy(x => x).toSeq.map {case (k, v) => k -> v.size.toDouble}
  
    (lowPriority ++ highPriority)
      .groupBy {case (k, _ ) => k}
      .map     {case (k, vs) => k -> vs.map(_._2).sum}
      .filter  {case (k, _ ) => k != wallOwnerId}
      .toSeq
  }

}
