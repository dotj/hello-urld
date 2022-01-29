package models

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec

import java.net.URI
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class ShortLinkManagerSpec extends PlaySpec with ScalaFutures {

  private val shortLinkRepo = mock(classOf[ShortLinkRepository])
  private val analyticsRepo = mock(classOf[AnalyticsRepository])
  private val manager = new ShortLinkManager(shortLinkRepo, analyticsRepo)

  private val url = new URI("https://google.com")
  private val token = "ggg"
  private val date = LocalDate.now()
  private val shortLink =
    ShortLink(id = 1, redirectToUrl = url.toString, token = token, expirationDate = Some(date))

  private val links = Seq(shortLink)

  "getAll" should {
    "return an empty list if no shortlinks are available" in {
      when(shortLinkRepo.getAll()) thenReturn successful(Seq.empty)
      manager.findAll().futureValue mustBe Seq.empty
    }

    "return a list of shortlinks if available" in {
      when(shortLinkRepo.getAll()) thenReturn successful(links)
      manager.findAll().futureValue mustBe links
    }
  }

  "findShortLinkByToken" should {
    "return a shortlink if it exists" in {
      when(shortLinkRepo.findByToken(token)) thenReturn successful(Some(shortLink))
      manager.findByToken(token).futureValue mustBe Some(shortLink)
    }

    "return None if not found" in {
      when(shortLinkRepo.findByToken(token)) thenReturn successful(None)
      manager.findByToken(token).futureValue mustBe None
    }
  }

  "createShortLink" should {
    "create a new short link" in {
      when(shortLinkRepo.findByToken(any[String])) thenReturn successful(None)
      when(shortLinkRepo.findByUrl(any[String])) thenReturn successful(None)
      when(shortLinkRepo.create(url.toString, token, Some(date))) thenReturn successful(shortLink)
      manager.createShortLink(url.toString, Some(token), Some(date)).futureValue mustBe shortLink
    }

    "validate URLs" in {
      val invalidUrl = "aaaaaaa"
      val thrown = intercept[Exception] {
        manager.createShortLink(invalidUrl, Some(token), Some(date))
      }
      assert(thrown.getMessage == s"Provided URI $invalidUrl could not be validated.")
    }

    "validate that tokens consist only of alphanumeric characters, dashes, and underscores" in {
      val invalidToken = "ohnoooo/@!"
      val thrown = intercept[Exception] {
        manager.createShortLink(url.toString, Some(invalidToken), None)
      }
      assert(thrown.getMessage == s"Provided token $invalidToken could not be validated.")

    }

    "create a random token if user does not provide a token" in {
      when(shortLinkRepo.findByToken(any[String])) thenReturn successful(None)
      when(shortLinkRepo.findByUrl(any[String])) thenReturn successful(None)

      when(shortLinkRepo.create(any[String], any[String], any[Option[LocalDate]])) thenReturn successful(shortLink)
      manager.createShortLink(url.toString, None, None).futureValue
    }

    "not create the shortlink if the URL already exists in the db" in {
      when(shortLinkRepo.findByToken(any[String])) thenReturn successful(None)
      when(shortLinkRepo.findByUrl(any[String])) thenReturn successful(Some(shortLink))

      val result = manager.createShortLink(url.toString, Some(token), None).failed.futureValue
      result.getMessage mustBe s"A shortlink for token $token or url ${url.toString} already exists."
    }

    "not create the shortlink if the token already exists in the db" in {
      when(shortLinkRepo.findByToken(any[String])) thenReturn successful(Some(shortLink))
      when(shortLinkRepo.findByUrl(any[String])) thenReturn successful(None)

      val result = manager.createShortLink(url.toString, Some(token), None).failed.futureValue
      result.getMessage mustBe s"A shortlink for token $token or url ${url.toString} already exists."
    }
  }

  "delete" should {
    "delegate to the repo to delete by token" in {
      when(shortLinkRepo.delete(token)) thenReturn successful(())
      manager.delete(token).futureValue
    }
  }

  "deprecate" should {
    "delegate to the repo to mark expired shortlinks" in {
      when(shortLinkRepo.deprecate()) thenReturn successful(())
      manager.deprecate().futureValue
    }
  }

  "updateShortLinkByToken" should {
    "delegate to the repo to update the shortlink" in {
      when(shortLinkRepo.update(token, url.toString)) thenReturn successful(())
      manager.updateShortLinkByToken(token, url.toString).futureValue
    }
  }

  "redirect" should {
    "return the shortlink if exists, and increment the analytics counter" in {
      val analyticsCount = 5

      when(shortLinkRepo.findByToken(token)) thenReturn successful(Some(shortLink))
      when(analyticsRepo.getOrInitCount(token)) thenReturn successful(analyticsCount)
      when(analyticsRepo.updateCount(token, analyticsCount + 1)) thenReturn successful(())

      manager.findRedirect(token).futureValue mustBe Some(shortLink)
    }
  }

}
