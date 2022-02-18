package models

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.dtd.ValidationException

class ShortLinkManager @Inject() (shortLinkRepo: ShortLinkRepository, analyticsRepo: AnalyticsRepository)(implicit
    ec: ExecutionContext
) {

  val maxTokenLength = 255

  def findAll(): Future[Seq[ShortLink]] =
    shortLinkRepo.getAll()

  def findByToken(token: String): Future[Option[ShortLink]] =
    shortLinkRepo.findByToken(token)

  def createShortLink(
      redirectToUrl: String,
      optToken: Option[String],
      optExpirationDate: Option[LocalDate]
  ): Future[ShortLink] = {
    val validUrl = validateUri(redirectToUrl)
    val validToken = validateOrCreateToken(optToken)
    for {
      _ <- ensureNoExistingShortLink(validToken, validUrl)
      created <- shortLinkRepo.create(validUrl, validToken, optExpirationDate)
    } yield created
  }

  def delete(token: String): Future[Unit] =
    shortLinkRepo.delete(token)

  def deprecate(): Future[Unit] =
    shortLinkRepo.deprecate()

  // TODO - should have more validation and options
  def updateShortLinkByToken(token: String, newRedirectToUrl: String): Future[Unit] =
    shortLinkRepo.update(token, newRedirectToUrl)

  def findRedirect(token: String): Future[Option[ShortLink]] =
    shortLinkRepo
      .findByToken(token)
      .map(shortLink => {
        incrementHitCounter(token)
        shortLink
      })

  def findAnalytics(token: String): Future[Long] =
    analyticsRepo.getOrInitCount(token)

  // TODO -
  //  Mutating updates is not supported in Slick so we're querying the db twice,
  //  which isn't ideal. A query in an `increment` method could be better.
  //  https://github.com/slick/slick/issues/497
  private def incrementHitCounter(token: String): Future[Unit] =
    for {
      current <- analyticsRepo.getOrInitCount(token)
      _ <- analyticsRepo.updateCount(token, current + 1)
    } yield ()

  private def ensureNoExistingShortLink(token: String, url: String): Future[Unit] =
    for {
      foundByToken <- shortLinkRepo.findByToken(token)
      foundByUrl <- shortLinkRepo.findByUrl(url)
    } yield
      if (foundByToken.isEmpty && foundByUrl.isEmpty) {
        Future.successful(())
      } else {
        Future.failed(throw ValidationException(s"A shortlink for token $token or url $url already exists."))
      }

  // TODO -
  //  Find/write a util that does a more thorough validation.
  //  https://stackoverflow.com/questions/19267856/play-framework-url-validator
  private def validateUri(rawUri: String): String = {
    val regex = "((http|https)://)(www.)?" +
      "[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]" +
      "{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)"
    if (rawUri.matches(regex)) {
      rawUri
    } else {
      throw ValidationException(s"Provided URI $rawUri could not be validated.")
    }
  }

  private def validateOrCreateToken(optToken: Option[String]): String =
    if (optToken.isDefined) {
      val rawToken = optToken.get
      val regex = "^[a-zA-Z0-9\\_\\-]{3,}$"
      if (rawToken.length < maxTokenLength && rawToken.matches(regex)) {
        rawToken
      } else {
        throw ValidationException(s"Provided token $rawToken could not be validated.")
      }
    } else {
      generateToken()
    }

  // Default of 10 chars for now
  private def generateToken(length: Int = 10): String = {
    val alphaNumericChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    val sb = new StringBuilder

    (1 to length).foreach(_ => {
      val randomNum = util.Random.nextInt(alphaNumericChars.length)
      sb.append(alphaNumericChars(randomNum))
    })

    sb.toString
  }

}
