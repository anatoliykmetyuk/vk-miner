package vkminer.serialize

import java.io.InputStream

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
      FileUtils.writeStringToFile(file(name), csv, "UTF-8")
    }

    override def deserialize(name: String): Map[String, (String, String)] =
      deserialize(FileUtils.openInputStream(file(name)))

    def deserialize(is: InputStream, encoding: String = "UTF-8"): Map[String, (String, String)] = {
      val pat = """(\d+),(\d+),(\d+)""".r
      IOUtils.readLines(is, encoding).map {case pat(uid, coId, ciId) => uid -> (coId -> ciId)}.toMap
    }

  }

}