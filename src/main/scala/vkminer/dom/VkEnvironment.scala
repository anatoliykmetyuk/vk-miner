package vkminer
package dom

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait VkEnvironment extends Extractors
                       with UserComponent
                       with LocationComponent
                       with GraphComponent {
  val api: VkApi

  implicit val jsonFormats = org.json4s.DefaultFormats
}

trait Extractors {this: VkEnvironment =>
  def extractXml (id: String)(implicit node: Node  ): Option[String] = (node \ id).headOption.map(_.text)
  def extractJson(id: String)(implicit node: JValue): Option[String] = (node \ id).toOption.map(_.values.toString)
}