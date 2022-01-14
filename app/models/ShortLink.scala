package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class ShortLink(
    id: Long,
    redirectToUrl: String,
    token: String,
    expirationDate: Option[LocalDate] = None,
    expired: Boolean = false
)

object ShortLink {
  implicit val slFormat: OFormat[ShortLink] = Json.format[ShortLink]
}

case class ShortLinkDto(
    redirectToUrl: String,
    token: String,
    expirationDate: Option[LocalDate] = None
)

object ShortLinkDto {
  implicit val slDtoFormat: OFormat[ShortLinkDto] = Json.format[ShortLinkDto]
}
