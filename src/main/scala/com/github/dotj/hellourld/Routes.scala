package com.github.dotj.hellourld

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.github.dotj.hellourld.ShortLinkRegistry._

import scala.concurrent.Future

class Routes(registry: ActorRef[ShortLinkRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  private def findShortLinks(): Future[ShortLinksDto] = registry.ask(FindAll)
  private def findShortLink(alias: String): Future[ShortLinkFoundResponse] = registry.ask(Find(alias, _))
  private def createShortLink(link: ShortLinkDto): Future[ActionPerformed] = registry.ask(Create(link, _))
  private def deleteShortLink(alias: String): Future[ActionPerformed] = registry.ask(Delete(alias, _))
  private def updateShortLink(alias: String, updateShortLinkRequest: UpdateShortLinkRequest): Future[ActionPerformed] =
    registry.ask(Update(alias, updateShortLinkRequest, _))

  private def shortcutBase = "s"
  private def shortLinkBase = "shortlink"

  private val getRedirect: Route =
    (get & path(shortcutBase / Segment)) { pathId =>
      rejectEmptyResponse {
        onSuccess(findShortLink(pathId)) { response =>
          response.shortLink match {
            case Some(link) => redirect(link.redirectToUrl, StatusCodes.TemporaryRedirect)
            case _          => complete(StatusCodes.NotFound)
          }
        }
      }
    }

  private val getShortLinks: Route =
    (get & path(shortLinkBase)) {
      complete(findShortLinks())
    }

  private val postShortLink: Route =
    (post & path(shortLinkBase)) {
      entity(as[ShortLinkDto]) { shortLink =>
        onSuccess(createShortLink(shortLink)) { performed =>
          complete((StatusCodes.Created, performed))
        }
      }
    }

  private val getShortLink: Route =
    (get & path(shortLinkBase / Segment)) { pathId =>
      rejectEmptyResponse {
        onSuccess(findShortLink(pathId)) { response =>
          complete(response.shortLink)
        }
      }
    }

  private val deleteShortLink: Route =
    (delete & path(shortLinkBase / Segment)) { pathId =>
      onSuccess(deleteShortLink(pathId)) { performed =>
        complete((StatusCodes.OK, performed))
      }
    }

  private val putShortLink: Route =
    (put & path(shortLinkBase / Segment)) { pathId =>
      entity(as[UpdateShortLinkRequest]) { request =>
        {
          onSuccess(updateShortLink(pathId, request)) { performed =>
            complete((StatusCodes.OK, performed))
          }
        }
      }
    }

  val allRoutes: Route = concat(
    getRedirect,
    getShortLinks,
    postShortLink,
    getShortLink,
    deleteShortLink,
    putShortLink,
    getRedirect
  )

}
