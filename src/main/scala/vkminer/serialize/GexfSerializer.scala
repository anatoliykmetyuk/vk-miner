package vkminer.serialize

import scala.xml._
import org.apache.commons.io._

import vkminer.dom.VkEnvironment

trait GexfSerializerComponent extends SerializerComponent {this: VkEnvironment =>

  object GexfSerializer extends Serializer[Graph] {
    val extension = "gexf"

    override def serialize  (graph: Graph, name: String) {
      val bom    = "\uFEFF"
      val header = """<?xml version="1.0" encoding="UTF-8"?>"""
      val xmlRaw = produceXml(graph)

      val pp  = new PrettyPrinter(200, 2)
      val xml = bom + header + "\n" + pp.format(xmlRaw)
      FileUtils.writeStringToFile(file(name), xml)
    }

    def produceXml(graph: Graph): Node =
        <gexf xmlns="http://www.gexf.net/1.2draft" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd" version="1.2">
          <graph defaultedgetype="undirected">
            <attributes class="node">
              <attribute id="1" title="Type" type="string"/>

              <attribute id="2" title="First name" type="string"/>
              <attribute id="3" title="Last name" type="string"/>

              <attribute id="4" title="Sex" type="string"/>
              <attribute id="5" title="Bitrhday" type="string"/>
              <attribute id="6" title="Age" type="float"/>
              <attribute id="7" title="Occupation" type="string"/>
              <attribute id="8" title="Home town" type="string"/>

              <attribute id="9"  title="Location name" type="string"/>
              <attribute id="10" title="Location type" type="string"/>
            </attributes>
            <nodes>
              {graph.nodes.map {
                case u: User =>
                  <node id={u.id} label={s"${u.firstName} ${u.lastName}"}>
                    <attvalues>
                      <attvalue for="1"  value="user"/>

                      <attvalue for="2"  value={u.firstName}/>
                      <attvalue for="3"  value={u.lastName}/>

                      <attvalue for="4"  value={if (u.sex == 1) "Female" else if (u.sex == 2) "Male" else "Unspecified"}/>
                      <attvalue for="5"  value={u.birthday.getOrElse("")}/>
                      <attvalue for="6"  value={u.age.map(_.toString).getOrElse("")}/>
                      <attvalue for="7"  value={u.occupation.getOrElse("")}/>
                      <attvalue for="8"  value={u.homeTown.getOrElse("")}/>

                      <attvalue for="9"  value=""/>
                      <attvalue for="10" value=""/>
                    </attvalues>
                  </node>

                case l: Location =>
                  <node id={l.id} label={l.name}>
                    <attvalues>
                      <attvalue for="1"  value="location"/>

                      <attvalue for="2"  value=""/>
                      <attvalue for="3"  value=""/>

                      <attvalue for="4"  value=""/>
                      <attvalue for="5"  value=""/>
                      <attvalue for="6"  value=""/>
                      <attvalue for="7"  value=""/>
                      <attvalue for="8"  value=""/>

                      <attvalue for="9"  value={l.name}/>
                      <attvalue for="10" value={l.tpe}/>
                    </attvalues>
                  </node>                  
              }}
            </nodes>
            <edges>
              {graph.edges.zipWithIndex.map {case (Edge(sid, tid, w), i) =>
                <edge id={i.toString} source={sid} target={tid} weight={w.toString}/>
              }}
            </edges>
          </graph>
        </gexf>

  }

}