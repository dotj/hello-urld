package models

import org.scalacheck.Gen
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import utils.Extensions.RichFuture
import utils.{DatabaseCleanerOnEachTest, InMemoryDatabaseWordSpec}

import java.time.LocalDate

class ShortLinkRepositorySpec
    extends InMemoryDatabaseWordSpec
    with DatabaseCleanerOnEachTest
    with TableDrivenPropertyChecks
    with Matchers {

  private val shortLinkRepo = app.injector.instanceOf[ShortLinkRepository]

  "create" should {
    "add the shortlink to db" in {
      genShortLink().map { link =>
        val created = shortLinkRepo.create(link.redirectToUrl, link.token, link.expirationDate).awaitForResult
        val found = shortLinkRepo.findByToken(link.token).awaitForResult

        created mustBe link
        found mustBe Some(link)
      }
    }
  }

  "delete" should {
    "should delete the shortlink if found" in {
      genShortLink().map { link =>
        shortLinkRepo.create(link.redirectToUrl, link.token, link.expirationDate).awaitForResult
        shortLinkRepo.delete(link.token).awaitForResult
        val found = shortLinkRepo.findByToken(link.token)

        found mustBe None
      }
    }
  }

  "find" should {
    "return None if the token is not found" in {
      genToken.map { token =>
        val found = shortLinkRepo.findByToken(token).awaitForResult
        found mustBe None
      }
    }

    "return None if the URL is not found" in {
      genUrl.map { url =>
        val found = shortLinkRepo.findByUrl(url).awaitForResult
        found mustBe None
      }
    }
  }

  "deprecate" should {
    "should find all rows with a passed expiration date and mark as 'expired'" in {
      genShortLink(expirationDate = Some(LocalDate.of(2000, 10, 15))).map { link =>
        shortLinkRepo.create(link.redirectToUrl, link.token, link.expirationDate).awaitForResult
        shortLinkRepo.deprecate().awaitForResult
        val found = shortLinkRepo.findByToken(link.token).awaitForResult

        found.map(_.expired) mustBe true
      }
    }
  }

  private def genShortLink(expirationDate: Option[LocalDate] = None): Gen[ShortLink] =
    for {
      id <- Gen.long
      redirectToUrl <- genUrl
      token <- genToken
    } yield ShortLink(id, redirectToUrl, token, expirationDate, expired = false)

  private def genToken: Gen[String] = Gen.alphaNumStr.suchThat(_.length < 10)

  private def genUrl: Gen[String] = "https://" + Gen.alphaNumStr.suchThat(_.length < 10) + ".com"

}
