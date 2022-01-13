package com.github.dotj.hellourld

import akka.actor.typed.scaladsl.AskPattern.{Askable, _}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.github.dotj.hellourld.ShortLinkRegistry.{Create, Delete, Find, FindAll, Update}

import scala.concurrent.Future

class ShortLinkManager(registry: ActorRef[ShortLinkRegistry.Command])(implicit val system: ActorSystem[_]) {

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def findShortLinks(): Future[ShortLinksDto] =
    registry.ask(FindAll)

  def findShortLink(alias: String): Future[ShortLinkFoundResponse] =
    registry.ask(Find(alias, _))

  def createShortLink(request: CreateShortLinkRequest): Future[SuccessResponse] =
    registry.ask(Create(request, _))

  def deleteShortLink(alias: String): Future[SuccessResponse] =
    registry.ask(Delete(alias, _))

  def updateShortLink(alias: String, updateShortLinkRequest: UpdateShortLinkRequest): Future[SuccessResponse] =
    registry.ask(Update(alias, updateShortLinkRequest, _))

}
