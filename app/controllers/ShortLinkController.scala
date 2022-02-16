package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import java.time.LocalDate
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

import java.util.Base64
import java.nio.charset.StandardCharsets

case class CreateShortLinkForm(redirectToUrl: String, token: Option[String], expirationDate: Option[LocalDate])
case class UpdateShortLinkForm(redirectToUrl: String, expirationDate: Option[LocalDate])

class ShortLinkController @Inject() (
    repo: ShortLinkRepository,
    analyticsRepo: AnalyticsRepository,
    cc: MessagesControllerComponents
)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index(createShortLinkForm))
  }

  // Base64.getEncoder.encodeToString("user:pass".getBytes(StandardCharsets.UTF_8))
  val user = "myuser"
  val pass = "mypass"
  val encodedAuth = Base64.getEncoder.encodeToString(s"$user:$pass".getBytes(StandardCharsets.UTF_8))

  // TODO
  // add role
  def addShortLink: Action[AnyContent] = Action.async { implicit request =>
    createShortLinkForm
      .bindFromRequest()
      .fold(
        err => {
          Future.successful(BadRequest(s"$err"))
        },
        req => {
          // Play's built-in request validators are synchronous, but we also
          // want to check the DB for existing URLs/tokens, so we have to do
          // some extra error handling here
          val result = for {
            existingUrl <- repo.findByUrl(req.redirectToUrl)
            token = req.token.getOrElse(generateToken())
            existingToken <- repo.findByToken(token)
            if existingUrl.isEmpty && existingToken.isEmpty
            created <- repo.create(req.redirectToUrl, token, req.expirationDate)
            dto = toDto(created)
          } yield Created(Json.toJson(dto))

          result.recover(_ => BadRequest("URL or token already exists"))
        }
      )
  }

  def getShortLinks: Action[AnyContent] = Action.async { implicit request =>
    for {
      allLinks <- repo.getAll()
      dto = allLinks.map(toDto)
    } yield Ok(Json.toJson(dto))
  }

  def getShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      link <- repo.findByToken(token)
      dto = link.map(toDto)
    } yield Ok(Json.toJson(dto))
  }

  def updateShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    updateShortLinkForm
      .bindFromRequest()
      .fold(
        err => {
          Future.successful(NotFound(s"$err"))
        },
        req => {
          repo
            .update(token, req.redirectToUrl)
            .map(_ => Ok(s"Update successful"))
        }
      )
  }

  // TODO
  def deleteShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    val authToken: String = request.headers.get("Authorization").get.split(" ")(1) // should be bXl1c2VyOm15cGFzcw==
    if (authToken == encodedAuth) {
      // Base64.getEncoder.encodeToString("user:pass".getBytes(StandardCharsets.UTF_8))
      repo
        .delete(token)
        .map(_ => Ok(s"Delete successful"))
    } else {
      Future(Unauthorized)
    }
  }

  // TODO -
  //   Currently, we have a way of specifying expiration dates for shortlinks,
  //   but they won't actually expire unless we manually hit this endpoint.
  //   If we don't expect a ton of traffic, one way we could automate the
  //   expiration is by hitting this endpoint via a cron job.
  def deprecateExpiredShortLinks: Action[AnyContent] = Action.async { implicit request =>
    repo
      .deprecate()
      .map(_ => Ok(s"Expired shortlinks successfully deprecated"))
  }

  def redirectByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    repo
      .findByToken(token)
      .map {
        case Some(link) =>
          incrementHitCounter(token)
          TemporaryRedirect(link.redirectToUrl)
        case _ => NotFound(s"Shortlink with token: $token not found")
      }
  }

  def getAnalytics(token: String): Action[AnyContent] = Action.async { implicit request =>
    analyticsRepo
      .getOrInitCount(token)
      .map(count => Ok(Json.toJson(count)))
  }

  // TODO -
  //  Mutating updates is not supported in Slick so we're querying the db twice,
  //  which isn't ideal. A query in an `increment` method could be better.
  //  https://github.com/slick/slick/issues/497
  private def incrementHitCounter(token: String): Future[Unit] =
    for {
      current <- analyticsRepo.getOrInitCount(token)
      _ <- analyticsRepo.updateCount(token, current + 1)
    } yield ()

  private val createShortLinkForm: Form[CreateShortLinkForm] =
    Form(
      mapping(
        "redirectToUrl" -> nonEmptyText.verifying("Malformed URL", u => validateUri(u)),
        "token" -> optional(text), // TODO - add more token validation
        "expirationDate" -> optional(localDate)
      )(CreateShortLinkForm.apply)(CreateShortLinkForm.unapply)
    )

  private val updateShortLinkForm: Form[UpdateShortLinkForm] =
    Form(
      mapping(
        "redirectToUrl" -> nonEmptyText,
        "expirationDate" -> optional(localDate)
      )(UpdateShortLinkForm.apply)(UpdateShortLinkForm.unapply)
    )

  // TODO -
  //  Find/write a util that does a more thorough validation.
  //  https://stackoverflow.com/questions/19267856/play-framework-url-validator
  private def validateUri(str: String): Boolean = {
    val regex = "((http|https)://)(www.)?" +
      "[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]" +
      "{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)"
    str.matches(regex)
  }

  private def generateToken(length: Int = 10): String = {
    val alphaNumericChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    val sb = new StringBuilder

    (1 to length).foreach(_ => {
      val randomNum = util.Random.nextInt(alphaNumericChars.length)
      sb.append(alphaNumericChars(randomNum))
    })

    sb.toString
  }

  private def toDto(model: ShortLink): ShortLinkDto =
    ShortLinkDto(redirectToUrl = model.redirectToUrl, token = model.token, expirationDate = model.expirationDate)

}
