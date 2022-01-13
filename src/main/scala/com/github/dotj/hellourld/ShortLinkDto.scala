package com.github.dotj.hellourld

import scala.collection.immutable

final case class ShortLinkDto(token: String, redirectToUrl: String)
final case class ShortLinksDto(shortLinks: immutable.Seq[ShortLinkDto])
final case class UpdateShortLinkRequest(newUrl: String)

object DtoTransformers {

  def toDto(domain: ShortLink): ShortLinkDto =
    ShortLinkDto(
      token = domain.token,
      redirectToUrl = domain.redirectTo.getPath
    )

  def toDto(links: Seq[ShortLink]): ShortLinksDto =
    ShortLinksDto(links.map(toDto))

}
