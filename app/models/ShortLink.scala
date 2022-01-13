package models

import play.api.libs.json.{Json, OFormat}

case class ShortLink(id: Long, redirectToUrl: String, token: String)

object ShortLink {
  implicit val slFormat: OFormat[ShortLink] = Json.format[ShortLink]
}
