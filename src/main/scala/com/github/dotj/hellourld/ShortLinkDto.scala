package com.github.dotj.hellourld

import scala.collection.immutable

final case class ShortLinkDto(redirectToUrl: String, token: String)
final case class ShortLinksDto(shortLinks: immutable.Seq[ShortLinkDto])

final case class CreateShortLinkRequest(redirectToUrl: String, token: Option[String])
final case class UpdateShortLinkRequest(redirectToUrl: String)

final case class ShortLinkFoundResponse(shortLink: Option[ShortLinkDto])
final case class SuccessResponse(description: String)
