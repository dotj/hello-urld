package com.github.dotj.hellourld

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ShortLinkRegistry {

  sealed trait Command
  final case class FindAll(replyTo: ActorRef[ShortLinks]) extends Command
  final case class Create(shortLink: ShortLink, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class Find(alias: String, replyTo: ActorRef[ShortLinkFoundResponse]) extends Command
  final case class Delete(alias: String, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class Update(alias: String, updateRequest: UpdateShortLinkRequest, replyTo: ActorRef[ActionPerformed])
      extends Command

  final case class ShortLinkFoundResponse(shortLink: Option[ShortLink])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(shortLinks: Set[ShortLink]): Behavior[Command] =
    Behaviors.receiveMessage {
      case FindAll(replyTo) =>
        replyTo ! ShortLinks(shortLinks.toSeq)
        Behaviors.same
      case Create(link, replyTo) =>
        replyTo ! ActionPerformed(s"Shortlink ${link.alias} created.")
        registry(shortLinks + link)
      case Find(token, replyTo) =>
        replyTo ! ShortLinkFoundResponse(shortLinks.find(_.alias == token))
        Behaviors.same
      case Delete(alias, replyTo) =>
        replyTo ! ActionPerformed(s"ShortLink $alias deleted.")
        registry(shortLinks.filterNot(_.alias == alias))
      case Update(alias, updateShortLink, replyTo) =>
        replyTo ! ActionPerformed(s"Shortlink $alias updated.")
        registry(shortLinks.filterNot(_.alias == alias) + ShortLink(alias, updateShortLink.updatedUrl))
    }
}
