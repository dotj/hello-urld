package controllers

import models.ShortLinkManager
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.specs2.mock.Mockito.mock
import play.api.mvc.MessagesControllerComponents
import play.api.test._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// TODO -
//  I haven't figured out how to set up these tests yet, so for now
//  I've stubbed out what behaviors I want to check for.
class ShortLinkControllerSpec extends PlaySpecification with ScalaFutures with IntegrationPatience {

  private lazy val manager = mock[ShortLinkManager]
  private lazy val cc = mock[MessagesControllerComponents]
  private lazy val controller = new ShortLinkController(manager, cc)(mock[ExecutionContext])

}
