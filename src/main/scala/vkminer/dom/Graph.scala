package vkminer
package dom

import scala.language.implicitConversions

import scala.xml._

trait GraphComponent {this: VkEnvironment =>

  trait GraphNode {
    val id: String
    def toXml: Node

    override def equals(that: Any): Boolean =
      if (!that.isInstanceOf[GraphNode]) false
      else that.asInstanceOf[GraphNode].id == id

    override def hashCode: Int = id.hashCode
  }

  case class Graph(nodes: Seq[GraphNode], edges: Seq[Edge]) {
    def toXml: Node =
      <graph>
        <nodes>
          {for (n <- nodes) yield n.toXml}
        </nodes>
        <edges>
          {for (e <- edges) yield e.toXml}
        </edges>
      </graph> 
  }

  object Graph {
    def xml2node(xml: Node): GraphNode = xml.label match {
      case "user"     => User    (xml)
      case "location" => Location(xml)
    }

    def apply(node: Node): Graph = {
      val nodes = (node \ "nodes" \ "_").map(xml2node)
      val edges = (node \ "edges" \ "_").map(Edge(_))
      Graph(nodes, edges)
    }
  }


  case class Edge(sourceId: String, targetId: String) {
    def toXml =
      <edge>
        <source>{sourceId}</source>
        <target>{targetId}</target>
      </edge>
  }

  object Edge {
    def apply(implicit node: Node): Edge = Edge(
      sourceId = extractXml("source").get
    , targetId = extractXml("target").get
    )

    def undirected(sourceId: String, targetId: String) = {
      if (sourceId.hashCode < targetId.hashCode) Edge(sourceId, targetId)
      else                                       Edge(targetId, sourceId)
    }

    def uOneToMany(source: GraphNode, targets: Seq[GraphNode]): Seq[Edge] =
      targets.map(t => Edge(source.id, t.id))
  }

}