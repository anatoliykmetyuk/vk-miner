package vkminer

import org.json4s._
import org.json4s.native.JsonMethods._

import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._
import org.apache.http.util._

import org.apache.commons.io._


class VkApi(val maybeToken: Option[String]) {
  System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
  System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
  System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
  System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

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

  @scala.annotation.tailrec
  final def method(name: String, args: Map[String, String]): JValue = {
    implicit val fmts = org.json4s.DefaultFormats

    val urlArgs = args.map {case (k, v) => s"$k=$v"}.mkString("&")
    val link = maybeToken.map {token =>
      s"https://api.vk.com/method/$name?$urlArgs&access_token=$token"
    }.getOrElse {
      s"https://api.vk.com/method/$name?$urlArgs"
    }

    val json = jsonGet(link)
    (json \ "error" \ "error_code").toOption.map(_.extract[Int]) match {
      case Some(6) => Thread.sleep(500); method(name, args)    // 6 is an error code for "too many requests per second error"
      case _       => json
    }
  }

}