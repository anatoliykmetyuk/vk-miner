package vkminer

import org.json4s._
import org.json4s.native.JsonMethods._

import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._
import org.apache.http.util._

import org.apache.commons.io._


class VkApi(val token: String) {

  def get(url: String): String = {
    val httpclient = HttpClients.createDefault()
    val httpGet    = new HttpGet(url)
    val response   = httpclient.execute(httpGet)

    var content: String = null
    try {
      val entity  = response.getEntity
      content = IOUtils.toString(entity.getContent)
      EntityUtils.consume(entity)
    } finally {
      response.close()
    }

    content
  }

  def jsonGet(url: String): JValue = parse(get(url))

  def method(name: String, args: Map[String, String]): JValue = {
    val urlArgs = args.map {case (k, v) => s"$k=$v"}.mkString("&")
    val link = s"https://api.vk.com/method/$name?$urlArgs&access_token=$token"
    jsonGet(link)
  }

}