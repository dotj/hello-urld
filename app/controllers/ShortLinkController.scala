package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import java.net.URI
import java.time.LocalDate
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

case class CreateShortLinkForm(redirectToUrl: String, token: Option[String], expirationDate: Option[LocalDate])
case class UpdateShortLinkForm(redirectToUrl: String, expirationDate: Option[LocalDate])

class ShortLinkController @Inject() (repo: ShortLinkRepository, cc: MessagesControllerComponents)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index(createShortLinkForm))
  }

  def addShortLink: Action[AnyContent] = Action.async { implicit request =>
    createShortLinkForm
      .bindFromRequest()
      .fold(
        err => {
          Future.successful(BadRequest(s"$err"))
        },
        createRequest => {
          val url = URI.create(createRequest.redirectToUrl)
          val token = createRequest.token.getOrElse(generateToken())
          val expiration = createRequest.expirationDate

          repo.create(url.toString, token, expiration).map { created =>
            Ok(Json.toJson(created))
          }
        }
      )
  }

  def getShortLinks: Action[AnyContent] = Action.async { implicit request =>
    for {
      allLinks <- repo.getAll()
      result = Ok(Json.toJson(allLinks))
    } yield result
  }

  def getShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      link <- repo.findByToken(token)
      result = Ok(Json.toJson(link))
    } yield result
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

  def deprecateShortLinkById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo
      .delete(id)
      .map(_ => Ok(s"Shortlink successfully deprecated"))
  }

  def deleteShortLinkById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo
      .delete(id)
      .map(_ => Ok(s"Shortlink successfully deleted"))
  }

  def redirectByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    repo
      .findByToken(token)
      .map {
        case Some(link) => TemporaryRedirect(link.redirectToUrl)
        case _          => NotFound(s"Token: $token not found")
      }
  }

  // Request form validation and mapping
  val createShortLinkForm: Form[CreateShortLinkForm] =
    Form(
      mapping(
        "redirectToUrl" -> nonEmptyText,
        "token" -> optional(text),
        "expirationDate" -> optional(localDate)
      )(CreateShortLinkForm.apply)(CreateShortLinkForm.unapply)
    )

  val updateShortLinkForm: Form[UpdateShortLinkForm] =
    Form(
      mapping(
        "redirectToUrl" -> nonEmptyText,
        "expirationDate" -> optional(localDate)
      )(UpdateShortLinkForm.apply)(UpdateShortLinkForm.unapply)
    )

  // TODO - maybe refactor this into a different class
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
