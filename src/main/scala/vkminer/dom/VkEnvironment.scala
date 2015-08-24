package vkminer
package dom

import scala.xml._
import org.json4s._
import org.json4s.native.JsonMethods._

trait VkEnvironment {
  val api: VkApi

  implicit val jsonFormats = org.json4s.DefaultFormats
}