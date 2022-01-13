package com.github.dotj.hellourld

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

class Routes(manager: ShortLinkManager) {

  import JsonParser._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import akka.http.scaladsl.server.Directives._

  private def shortcutBase = "s"
  private def shortLinkBase = "shortlink"

  private val getRedirect: Route =
    (get & path(shortcutBase / Segment)) { pathId =>
      rejectEmptyResponse {
        onSuccess(manager.findShortLink(pathId)) { response =>
          response.shortLink match {
            case Some(link) => redirect(link.redirectToUrl, StatusCodes.TemporaryRedirect)
            case _          => complete(StatusCodes.NotFound)
          }
        }
      }
    }

  private val getShortLinks: Route =
    (get & path(shortLinkBase)) {
      complete(manager.findShortLinks())
    }

  private val postShortLink: Route =
    (post & path(shortLinkBase)) {
      entity(as[CreateShortLinkRequest]) { shortLink =>
        onSuccess(manager.createShortLink(shortLink)) { performed =>
          complete((StatusCodes.Created, performed))
        }
      }
    }

  private val getShortLink: Route =
    (get & path(shortLinkBase / Segment)) { pathId =>
      rejectEmptyResponse {
        onSuccess(manager.findShortLink(pathId)) { response =>
          complete(response.shortLink)
        }
      }
    }

  private val deleteShortLink: Route =
    (delete & path(shortLinkBase / Segment)) { pathId =>
      onSuccess(manager.deleteShortLink(pathId)) { performed =>
        complete((StatusCodes.OK, performed))
      }
    }

  private val putShortLink: Route =
    (put & path(shortLinkBase / Segment)) { pathId =>
      entity(as[UpdateShortLinkRequest]) { request =>
        {
          onSuccess(manager.updateShortLink(pathId, request)) { performed =>
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
