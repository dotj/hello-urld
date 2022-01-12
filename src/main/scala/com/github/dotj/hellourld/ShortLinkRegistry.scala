package com.github.dotj.hellourld

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ShortLinkRegistry {

  sealed trait Command
  final case class FindAll(replyTo: ActorRef[ShortLinksDto]) extends Command
  final case class Create(shortLink: ShortLinkDto, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class Find(token: String, replyTo: ActorRef[ShortLinkFoundResponse]) extends Command
  final case class Delete(token: String, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class Update(token: String, updateRequest: UpdateShortLinkRequest, replyTo: ActorRef[ActionPerformed])
      extends Command

  final case class ShortLinkFoundResponse(shortLink: Option[ShortLinkDto])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(shortLinks: Set[ShortLinkDto]): Behavior[Command] =
    Behaviors.receiveMessage {
      case FindAll(replyTo) =>
        replyTo ! ShortLinksDto(shortLinks.toSeq)
        Behaviors.same
      case Create(shortLink, replyTo) =>
        replyTo ! ActionPerformed(s"ShortLink ${shortLink.token} created.")
        registry(shortLinks + shortLink)
      case Find(token, replyTo) =>
        replyTo ! ShortLinkFoundResponse(shortLinks.find(_.token == token))
        Behaviors.same
      case Delete(token, replyTo) =>
        replyTo ! ActionPerformed(s"ShortLink $token deleted.")
        registry(shortLinks.filterNot(_.token == token))
      case Update(token, updateShortLink, replyTo) =>
        replyTo ! ActionPerformed(s"ShortLink $token updated.")
        registry(
          shortLinks.filterNot(_.token == token) + ShortLinkDto(token = token, redirectToUrl = updateShortLink.newUrl)
        )
    }
}
