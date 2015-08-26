package vkminer.serialize

import scala.collection.JavaConversions._
import scala.language.implicitConversions

import org.apache.commons.io._

import vkminer.dom.VkEnvironment

trait UniversitiesSerializerComponent extends SerializerComponent {this: VkEnvironment =>

  object UniversitiesSerializer extends Serializer[Map[String, (String, String)]] {
    val extension = "csv"

    /** universtyId -> (countryId, cityId) */
    override def serialize(unis: Map[String, (String, String)], name: String) {
      val csv = unis.toSeq.map {case (uid, (coId, ciId)) => s"$uid,$coId,$ciId"}.mkString("\n")
      FileUtils.writeStringToFile(file(name), csv)
    }

    override def deserialize(name: String): Map[String, (String, String)] = {
      val pat = """(\d+),(\d+),(\d+)""".r
      FileUtils.readLines(file(name)).map {case pat(uid, coId, ciId) => uid -> (coId -> ciId)}.toMap
    }

  }

}