package vkminer.strategies
package generic

import scala.language.implicitConversions

import vkminer.dom.VkEnvironment

import org.json4s._
import org.json4s.native.JsonMethods._


trait BasicStrategy {
  type E <: VkEnvironment
  val e: E
}
