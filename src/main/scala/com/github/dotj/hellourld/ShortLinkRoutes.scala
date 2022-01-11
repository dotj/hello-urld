package com.github.dotj.hellourld

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import ShortLinkRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

class ShortLinkRoutes(registry: ActorRef[ShortLinkRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  private def findShortLinks(): Future[ShortLinks] = registry.ask(FindAll)
  private def findShortLink(alias: String): Future[ShortLinkFoundResponse] = registry.ask(Find(alias, _))
  private def createShortLink(link: ShortLink): Future[ActionPerformed] = registry.ask(Create(link, _))
  private def deleteShortLink(alias: String): Future[ActionPerformed] = registry.ask(Delete(alias, _))
  private def updateShortLink(alias: String, updateShortLinkRequest: UpdateShortLinkRequest): Future[ActionPerformed] =
    registry.ask(Update(alias, updateShortLinkRequest, _))

  val shortLinkRoutes: Route =
    pathPrefix("shortlink") {
      concat(
        pathEnd {
          concat(
            get {
              complete(findShortLinks())
            },
            post {
              entity(as[ShortLink]) { shortLink =>
                onSuccess(createShortLink(shortLink)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        path(Segment) { alias =>
          concat(
            get {
              rejectEmptyResponse {
                onSuccess(findShortLink(alias)) { response =>
                  complete(response.shortLink)
                }
              }
            },
            delete {
              onSuccess(deleteShortLink(alias)) { performed =>
                complete((StatusCodes.OK, performed))
              }
            },
            put {
              entity(as[UpdateShortLinkRequest]) { request =>
                onSuccess(updateShortLink(alias, request)) { performed =>
                  complete((StatusCodes.OK, performed))
                }
              }
            }
          )
        }
      )
    }

}
