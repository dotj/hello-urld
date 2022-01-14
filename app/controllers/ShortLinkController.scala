package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

case class CreateShortLinkForm(redirectToUrl: String, token: String)
case class UpdateShortLinkForm(redirectToUrl: String)

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
        errorForm => {
          Future.successful(Ok(views.html.index(errorForm)))
        },
        shortLink => {
          repo.create(shortLink.redirectToUrl, shortLink.token).map { _ =>
            Redirect(routes.ShortLinkController.index).flashing("success" -> "shortLink.created")
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
            .map(result => Ok(s"Update successful"))
        }
      )
  }

  def deleteShortLinkById(id: String): Action[AnyContent] = Action.async { implicit request =>
    repo
      .delete(id.toLong)
      .map(_ => Ok(s"Delete successful"))
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
        "redirectToUrl" -> nonEmptyText, // TODO validate inputs
        "token" -> nonEmptyText
      )(CreateShortLinkForm.apply)(CreateShortLinkForm.unapply)
    )

  val updateShortLinkForm: Form[UpdateShortLinkForm] =
    Form(
      mapping(
        "redirectToUrl" -> nonEmptyText
      )(UpdateShortLinkForm.apply)(UpdateShortLinkForm.unapply)
    )
}
