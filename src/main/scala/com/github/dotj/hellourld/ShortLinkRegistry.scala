package com.github.dotj.hellourld

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ShortLinkRegistry {

  sealed trait Command
  final case class FindAll(replyTo: ActorRef[ShortLinksDto]) extends Command
  final case class Create(request: CreateShortLinkRequest, replyTo: ActorRef[SuccessResponse]) extends Command
  final case class Find(token: String, replyTo: ActorRef[ShortLinkFoundResponse]) extends Command
  final case class Delete(token: String, replyTo: ActorRef[SuccessResponse]) extends Command
  final case class Update(token: String, updateRequest: UpdateShortLinkRequest, replyTo: ActorRef[SuccessResponse])
      extends Command

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(existingShortLinks: Set[ShortLinkDto]): Behavior[Command] =
    Behaviors.receiveMessage {
      case FindAll(replyTo) =>
        replyTo ! ShortLinksDto(existingShortLinks.toSeq)
        Behaviors.same

      case Create(request, replyTo) =>
        replyTo ! SuccessResponse(s"ShortLink for URL ${request.redirectToUrl} created.")
        registry(createNew(request, existingShortLinks))

      case Find(token, replyTo) =>
        replyTo ! ShortLinkFoundResponse(existingShortLinks.find(_.token == token))
        Behaviors.same

      case Delete(token, replyTo) =>
        replyTo ! SuccessResponse(s"ShortLink $token deleted.")
        registry(delete(token, existingShortLinks))

      case Update(token, updateShortLink, replyTo) =>
        replyTo ! SuccessResponse(s"ShortLink $token updated.")
        registry(update(token, updateShortLink, existingShortLinks))
    }

  // TODO - Refactor this to the manager or combine the manager/registry
  private def createNew(request: CreateShortLinkRequest, existing: Set[ShortLinkDto]): Set[ShortLinkDto] =
    existing + ShortLinkDto(
      redirectToUrl = request.redirectToUrl,
      token = request.token.getOrElse(generateToken())
    )

  private def generateToken(length: Int = 7): String = {
    val alphaNumericChars = ('a' to 'z') ++ ('0' to '9')
    val sb = new StringBuilder

    (1 to length).foreach(_ => {
      val randomNum = util.Random.nextInt(alphaNumericChars.length)
      sb.append(alphaNumericChars(randomNum))
    })

    sb.toString
  }

  private def delete(token: String, existing: Set[ShortLinkDto]): Set[ShortLinkDto] =
    existing.filterNot(_.token == token)

  private def update(token: String, request: UpdateShortLinkRequest, existing: Set[ShortLinkDto]): Set[ShortLinkDto] =
    existing.filterNot(_.token == token) + ShortLinkDto(token, request.redirectToUrl)

}
