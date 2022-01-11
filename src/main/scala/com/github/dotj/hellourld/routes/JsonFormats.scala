package com.github.dotj.hellourld.routes

import com.github.dotj.hellourld.registry.ShortLinkRegistry.ActionPerformed
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val shortLinkJsonFormat: RootJsonFormat[ShortLinkDto] = jsonFormat2(ShortLinkDto)
  implicit val shortLinksJsonFormat: RootJsonFormat[ShortLinksDto] = jsonFormat1(ShortLinksDto)
  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
  implicit val updateRequestFormat: RootJsonFormat[UpdateShortLinkRequest] = jsonFormat1(UpdateShortLinkRequest)

}
