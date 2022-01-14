package models

import play.api.libs.json.{Json, OFormat}

case class Analytics(
    id: Long,
    token: String,
    hitCount: Long
)

object Analytics {
  implicit val slFormat: OFormat[Analytics] = Json.format[Analytics]
}
