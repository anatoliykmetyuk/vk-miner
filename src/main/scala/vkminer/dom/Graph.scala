package vkminer
package dom


case class Graph(users: Seq[User], edges: Seq[Edge]) {
  def toXml: Node =
<network>
  <users>
    {for (u <- users) yield u.toXml}
  </users>
  <edges>
    {for (e <- edges) yield e.toXml}
  </edges>
</network> 
}

object Graph {
  def apply(node: Node): Graph = {
    val users = (node \ "users" \ "_").map(User(_))
    val edges = (node \ "edges" \ "_").map(Edge(_))
    Graph(users, edges)
  }
}


trait EdgeComponent {
  class Edge(_id1: Long, _id2: Long) {
    val id1 = math.min(_id1, _id2)
    val id2 = math.max(_id1, _id2)

    override def equals(that: Any): Boolean = that match {
      case e: Edge => id1 == e.id1 && id2 == e.id2
      case _ => false
    }

    override def hashCode = (id1 + id2).toInt

    override def toString = s"Edge($id1, $id2)"

    def toXml =
      <edge>
        <id1>{id1}</id1>
        <id2>{id2}</id2>
      </edge>
  }

  object Edge {
    def apply(id1: Long, id2: Long): Edge = new Edge(id1, id2)
    def apply(node: Node): Edge = Edge(
      id1 = (node \ "id1").text.toLong
    , id2 = (node \ "id2").text.toLong
    )
  }
}
