package com.github.dotj.hellourld

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ShortLinkRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  private lazy val testKit = ActorTestKit()
  private implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  // Here we need to implement all the abstract members of ShortLinkRoutes.
  // We use the real ShortLinkRegistryRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  private val shortLinkRegistry = testKit.spawn(ShortLinkRegistry())
  private lazy val routes = new ShortLinkRoutes(shortLinkRegistry).shortLinkRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  "ShortLinkRoutes" should {
    "return no shortlinks if no present (GET /shortlink)" in {
      val request = HttpRequest(uri = "/shortlink")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"links":[]}"""
      }
    }

    "be able to add short links (POST /shortlink)" in {
      val shortLink = ShortLink(alias = "ggg", fullUrl = "google.com")
      val shortLinkEntity =
        Marshal(shortLink)
          .to[MessageEntity]
          .futureValue // futureValue is from ScalaFutures

      val request = Post("/shortlink").withEntity(shortLinkEntity)

      request ~> routes ~> check {
        status shouldBe StatusCodes.Created
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"description":"Shortlink ggg created."}"""
      }
    }

    "be able to edit short link (PUT /shortlink)" in {
      val request = Put(uri = "/shortlink/ggg", content = UpdateShortLinkRequest(updatedUrl = "lmgtfy.app"))

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"description":"Shortlink ggg updated."}"""
      }
    }

    "be able to remove short link (DELETE /shortlink)" in {
      val request = Delete(uri = "/shortlink/aaa")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"description":"ShortLink aaa deleted."}"""
      }
    }

  }

}
