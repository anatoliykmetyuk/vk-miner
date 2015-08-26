package vkminer.serialize

import scala.xml._
import org.apache.commons.io._

import vkminer.dom.VkEnvironment

trait XmlSerializerComponent extends SerializerComponent {this: VkEnvironment =>

  object XmlSerializer extends Serializer[Graph] {
    val extension = "xml"

    override def serialize  (graph: Graph, name: String) {
      val pp  = new PrettyPrinter(200, 2)
      val xml = pp format graph.toXml
      FileUtils.writeStringToFile(file(name), xml)
    }

    override def deserialize(name: String): Graph =
      Graph(XML loadFile file(name))
  }

}