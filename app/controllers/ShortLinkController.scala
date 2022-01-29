package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import java.time.LocalDate
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

case class CreateShortLinkForm(redirectToUrl: String, token: Option[String], expirationDate: Option[LocalDate])
case class UpdateShortLinkForm(redirectToUrl: String, expirationDate: Option[LocalDate])

class ShortLinkController @Inject() (manager: ShortLinkManager, cc: MessagesControllerComponents)(implicit
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
        req => {
          manager
            .createShortLink(req.redirectToUrl, req.token, req.expirationDate)
            .map(result => Created(Json.toJson(result)))
        }
      )
  }

  def getShortLinks: Action[AnyContent] = Action.async { implicit request =>
    manager
      .findAll()
      .map(result => Ok(Json.toJson(result)))
  }

  def getShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    manager
      .findByToken(token)
      .map(result => Ok(Json.toJson(result)))
  }

  def updateShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    updateShortLinkForm
      .bindFromRequest()
      .fold(
        err => {
          Future.successful(NotFound(s"$err"))
        },
        req => {
          manager
            .updateShortLinkByToken(token, req.redirectToUrl)
            .map(_ => Ok("Update successful"))
        }
      )
  }

  def deleteShortLinkByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    manager
      .delete(token)
      .map(_ => Ok("Delete successful"))
  }

  // TODO -
  //   Currently, we have a way of specifying expiration dates for shortlinks,
  //   but they won't actually expire unless we manually hit this endpoint.
  //   If we don't expect a ton of traffic, one way we could automate the
  //   expiration is by hitting this endpoint via a cron job.
  def deprecateExpiredShortLinks: Action[AnyContent] = Action.async { implicit request =>
    manager
      .deprecate()
      .map(_ => Ok("Expired shortlinks successfully deprecated"))
  }

  def redirectByToken(token: String): Action[AnyContent] = Action.async { implicit request =>
    manager
      .findRedirect(token)
      .map {
        case Some(link) => TemporaryRedirect(link.redirectToUrl)
        case _          => NotFound(s"Shortlink with token $token not found")
      }
  }

  def getAnalytics(token: String): Action[AnyContent] = Action.async { implicit request =>
    manager
      .findAnalytics(token)
      .map(count => Ok(Json.toJson(count)))
  }

  private val createShortLinkForm: Form[CreateShortLinkForm] =
    Form(
      mapping(
        "redirectToUrl" -> nonEmptyText,
        "token" -> optional(text),
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

}
