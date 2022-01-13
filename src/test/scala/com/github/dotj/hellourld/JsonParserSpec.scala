package com.github.dotj.hellourld

import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

// TODO
class JsonParserSpec extends AnyWordSpec {

  import spray.json._
  case class CreateShortLinkRequest(redirectToUrl: String, token: Option[String] = None)
  object MyProtocol extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(CreateShortLinkRequest)
  }

  import MyProtocol.format
  val req1 = CreateShortLinkRequest("foo", Some("bar"))
  val strJson1 = req1.toJson.toString

  val strJson2 = """{"redirectToUrl": "foo"}"""
  val req2 = strJson2.parseJson.convertTo[CreateShortLinkRequest]

  println(s"req1=$req1, req2=$req2")

  val x = Random.alphanumeric
  println(randomAlphaNumericString())

  // https://alvinalexander.com/scala/creating-random-strings-in-scala/
  // 1 - a java-esque approach
  def randomString(length: Int) = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to length) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }

  // 6 - random alphanumeric
  def randomAlphaNumericString(length: Int = 12): String = {
    val alphaNumericChars = ('a' to 'z') ++ ('0' to '9')
    val sb = new StringBuilder

    (1 to length).foreach(_ => {
      val randomNum = util.Random.nextInt(alphaNumericChars.length)
      sb.append(alphaNumericChars(randomNum))
    })

    sb.toString
  }

}
