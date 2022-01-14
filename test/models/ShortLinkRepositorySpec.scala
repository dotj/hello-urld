package models

import org.scalatest.Ignore
import org.specs2.mutable.Specification

// TODO -
//  Stubbed out tests for now.
//  The @Ignore tag ignores the tests and the `ok` is a placeholder

@Ignore
class ShortLinkRepositorySpec extends Specification {

  private lazy val dbConfig = ???
  private lazy val repo = new ShortLinkRepository(dbConfig)(???)

  "ShortLinkRepository" should {

    "create" should {
      "add the shortlink to db" in { ok }
    }

    "getAll" should {
      "return a list of shortlinks" in { ok }
      "return empty if none are found" in { ok }
    }

    "update" should {
      "should update the row in the db" in { ok }
    }

    "findByToken" should {
      "should return the shortlink if found" in { ok }
      "should return None if not found" in { ok }
    }

    "delete" should {
      "should delete the shortlink if found" in { ok }
    }

    "deprecate" should {
      "should find all rows with a passed expiration date and mark as 'expired'"
    }
  }
}
