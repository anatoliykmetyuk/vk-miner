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

  case class Graph(nodes: Set[GraphNode] = Set(), edges: Set[Edge] = Set()) {
    def toXml: Node =
      <graph>
        <nodes>
          {for (n <- nodes) yield n.toXml}
        </nodes>
        <edges>
          {for (e <- edges) yield e.toXml}
        </edges>
      </graph>

    /** Adds the node to this graph's set of nodes. */
    def +(n: GraphNode): Graph = Graph(nodes + n, edges)

    /** Computes the union of the two graphs. */
    def ++(g: Graph): Graph = Graph(nodes ++ g.nodes, edges ++ g.edges)
    
    /** Links this node to every node of this graph. */
    def ->:(node: GraphNode) =
      Graph(nodes + node, edges ++ nodes.map {n => Edge.undirected(node.id, n.id)})

    /** Links this node to every node of the specified type of this graph. */
    def -->:[T <: GraphNode](node: GraphNode)(implicit m: reflect.Manifest[T]) =
      Graph(nodes + node, edges ++ nodes.filter(m.runtimeClass.isInstance).map {n => Edge.undirected(node.id, n.id)})

    /** 
     * Removes the dead nodes (the ones with id's number "0") and
     * the dead edges (the ones who's source or destination is absent)
     */
    def sanitize: Graph = {
      val newNodes    = nodes.filter {x =>
        val num = x.id.drop(2)
        !num.isEmpty && num.toLong > 0
      }
      val newNodesIds = newNodes.map {_.id}
      val newEdges    = edges.filter {e => newNodesIds.contains(e.sourceId) && newNodesIds.contains(e.targetId)}
      Graph(newNodes, newEdges)
    }
  }

  object Graph {
    def xml2node(xml: Node): GraphNode = xml.label match {
      case "user"     => User    (xml)
      case "location" => Location(xml)
    }

    def apply(node: Node): Graph = {
      val nodes = (node \ "nodes" \ "_").map(xml2node)
      val edges = (node \ "edges" \ "_").map(Edge(_))
      Graph(nodes.toSet, edges.toSet)
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
  
    def uAll(nodes: Seq[GraphNode]): Seq[Edge] = nodes.flatMap {n1 =>
      nodes.filter(_ != n1).map {n2 => undirected(n1.id, n2.id)}
    }
  }

}