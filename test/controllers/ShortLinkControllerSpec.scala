package controllers

import models.{AnalyticsRepository, ShortLinkRepository}
import org.scalatest.Ignore
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.specs2.mock.Mockito.mock
import play.api.mvc.MessagesControllerComponents
import play.api.test._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

// TODO -
//  I haven't figured out how to set up these tests yet, so for now
//  I've stubbed out what behaviors I want to check for.
//  The @Ignore tag ignores the tests and the `ok` is a placeholder

@Ignore
class ShortLinkControllerSpec extends PlaySpecification with ScalaFutures with IntegrationPatience {

  private lazy val repo = mock[ShortLinkRepository]
  private lazy val analyticsRepo = mock[AnalyticsRepository]
  private lazy val cc = mock[MessagesControllerComponents]
  private lazy val controller = new ShortLinkController(repo, analyticsRepo, cc)(mock[ExecutionContext])

  "(GET getShortLinks)" should {
    "return a list of shortlinks" in {
      // verify Ok status code and data
      ok
    }
    "return an empty list if no shortlinks are found" in { ok }
  }

  "(GET getShortLinkByToken)" should {
    "return a shortlink if it exists" in { ok }
    "return a NotFound error otherwise" in { ok }
  }

  "(POST addShortLink)" should {
    "create a new shortlink" in { ok }
    "accept requests without a token or expiration date" in { ok }

    // validation tests
    "should validate URLs in the request" in { ok }

    // needs to be web-friendly characters
    "should validate tokens in the request" in { ok }
  }

  "(DELETE deleteShortLinkById)" should {
    "delete an existing shortlink" in { ok }
    "return a NotFound error if token does not exist" in { ok }
  }

  "(PUT deprecate)" should {
    "deprecate all expired shortlinks" in { ok }
  }

  "(PUT updateShortLinkByToken)" should {
    "update a shortlink" in { ok }
  }

  "(GET redirectByToken)" should {
    "redirect to the full url if the shortlink exists" in {
      // verify TemporaryRedirect status code and redirectToUrl
      ok
    }
  }
}
