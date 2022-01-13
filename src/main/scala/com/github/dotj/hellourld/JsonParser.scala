package com.github.dotj.hellourld

import com.github.dotj.hellourld.ShortLinkRegistry.SuccessResponse
import spray.json.{DefaultJsonProtocol, JsString, JsValue, NullOptions, RootJsonFormat}

object JsonParser extends DefaultJsonProtocol {
  implicit val shortLinkJsonFormat: RootJsonFormat[ShortLinkDto] = jsonFormat2(ShortLinkDto)
  implicit val shortLinksJsonFormat: RootJsonFormat[ShortLinksDto] = jsonFormat1(ShortLinksDto)
  implicit val successResponseFormat: RootJsonFormat[SuccessResponse] = jsonFormat1(SuccessResponse)
  implicit val createSLRequestFormat: RootJsonFormat[CreateShortLinkRequest] = jsonFormat2(CreateShortLinkRequest)
  implicit val updateSLRequestFormat: RootJsonFormat[UpdateShortLinkRequest] = jsonFormat1(UpdateShortLinkRequest)
}

//implicit object shortLinkDtoFormat extends RootJsonFormat[ShortLinkDto] {
//  override def read(json: JsValue): ShortLinkDto = {
//    json.asJsObject.getFields("token", "??") match {
//      case Seq(JsString(token), JsString(url)) => ShortLinkDto(token = token, redirectToUrl = url)
//    }
//  }
//}
