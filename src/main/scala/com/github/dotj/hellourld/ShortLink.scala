package com.github.dotj.hellourld

import scala.collection.immutable

final case class ShortLink(alias: String, fullUrl: String)
final case class ShortLinks(links: immutable.Seq[ShortLink])
final case class UpdateShortLinkRequest(updatedUrl: String)
