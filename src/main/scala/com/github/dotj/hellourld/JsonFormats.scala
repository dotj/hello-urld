package com.github.dotj.hellourld

import ShortLinkRegistry.ActionPerformed
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val shortLinkJsonFormat: RootJsonFormat[ShortLink] = jsonFormat2(ShortLink)
  implicit val shortLinksJsonFormat: RootJsonFormat[ShortLinks] = jsonFormat1(ShortLinks)
  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
  implicit val updateRequestFormat: RootJsonFormat[UpdateShortLinkRequest] = jsonFormat1(UpdateShortLinkRequest)

}
