package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class ShortLinkController @Inject() (repo: ShortLinkRepository, cc: MessagesControllerComponents)(implicit
    ec: ExecutionContext
) extends MessagesAbstractController(cc) {

  val shortLinkForm: Form[CreateShortLinkForm] = Form {
    mapping(
      "redirectToUrl" -> nonEmptyText, // TODO validate inputs
      "token" -> nonEmptyText
    )(CreateShortLinkForm.apply)(CreateShortLinkForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index(shortLinkForm))
  }

  def addShortLink: Action[AnyContent] = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    shortLinkForm
      .bindFromRequest()
      .fold(
        // The error function. We return the index page with the error form, which will render the errors.
        // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
        // a future because the short link creation function returns a future.
        errorForm => {
          Future.successful(Ok(views.html.index(errorForm)))
        },
        // There were no errors in the from, so create the short link.
        shortLink => {
          repo.create(shortLink.redirectToUrl, shortLink.token).map { _ =>
            // If successful, we simply redirect to the index page.
            Redirect(routes.ShortLinkController.index).flashing("success" -> "shortLink.created")
          }
        }
      )
  }

  def getShortLinks: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { shortLink =>
      Ok(Json.toJson(shortLink))
    }
  }
}

/** The create short link form.
  *
  * Generally for forms, you should define separate objects to your models, since forms very often need to present data
  * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
  * that is generated once it's created.
  */
case class CreateShortLinkForm(redirectToUrl: String, token: String)
