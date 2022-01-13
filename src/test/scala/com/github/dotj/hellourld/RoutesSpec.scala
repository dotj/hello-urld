package com.github.dotj.hellourld

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.dotj.hellourld.ShortLinkRegistry.{SuccessResponse, ShortLinkFoundResponse}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock

import java.net.URL
import scala.concurrent.Future

// TODO - make tests more thorough
class RoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

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
//  private val shortLinkRegistry = testKit.spawn(ShortLinkRegistry())
  private val shortLinkManager = mock[ShortLinkManager]
  private lazy val routes = new Routes(shortLinkManager).allRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import JsonParser._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // Testing values
  private val redirectToUrl = new URL("https://wikipedia.com")
  private val token = "wiki"
  private val shortLink = ShortLinkDto(token = token, redirectToUrl = redirectToUrl.toString)
  private val shortLinks = ShortLinksDto(Seq(shortLink))

  "shortlink routes" should {
    "return no shortlinks if no present (GET /shortlink)" in {
      when(shortLinkManager.findShortLinks()) thenReturn Future.successful(shortLinks)

      val request = HttpRequest(uri = "/shortlink")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"shortLinks":[[{"redirectToUrl":"","token":"wiki"}]]}"""
      }
    }

//    "be able to add short links (POST /shortlink)" in {
//      when(shortLinkManager.createShortLink(shortLink)) thenReturn Future.successful(
//        SuccessResponse("ShortLink wiki created.")
//      )
//
//      val shortLinkEntity = Marshal(shortLink).to[MessageEntity].futureValue
//
//      val request = Post("/shortlink").withEntity(shortLinkEntity)
//
//      request ~> routes ~> check {
//        status shouldBe StatusCodes.Created
//        contentType shouldBe ContentTypes.`application/json`
//        entityAs[String] shouldBe s"""{"description":"ShortLink $token created."}"""
//      }
//    }

    // TODO
    "be able to edit short link (PUT /shortlink)" in {
      val reqContent = UpdateShortLinkRequest(redirectToUrl = "https://lmgtfy.app")
      when(shortLinkManager.updateShortLink(alias = token, updateShortLinkRequest = reqContent)) thenReturn Future
        .successful(SuccessResponse("yay"))

      val request = Put(uri = s"/shortlink/$token", content = reqContent)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] should contain("yay")
      }
    }

    "be able to remove short link (DELETE /shortlink)" in {
      when(shortLinkManager.deleteShortLink(token)) thenReturn Future.successful(SuccessResponse("yay"))

      val request = Delete(uri = s"/shortlink/$token")

      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] should contain("yay")
      }
    }

//    "redirect route" should {
//      "redirect to the full url" in {
//        when(shortLinkManager.createShortLink(shortLink)) thenReturn Future.successful(SuccessResponse("yay"))
//        when(shortLinkManager.findShortLink(token)) thenReturn Future.successful(
//          ShortLinkFoundResponse(Some(shortLink))
//        )
//
//        // Create short link
//        val shortLinkEntity = Marshal(shortLink).to[MessageEntity].futureValue
//        val firstRequest = Post("/shortlink").withEntity(shortLinkEntity)
//        firstRequest ~> routes ~> check { status shouldBe StatusCodes.Created }
//
//        // Confirm short link will redirect
//        val secondRequest = HttpRequest(uri = s"/s/$token")
//
//        secondRequest ~> routes ~> check {
//          status shouldBe StatusCodes.TemporaryRedirect
//          // TODO - Add a more thorough check for redirection
//        }
//      }
//    }

  }

}
