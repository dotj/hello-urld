package com.github.dotj.hellourld

import java.net.URL
import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

import scala.concurrent._
import ExecutionContext.Implicits.global

class ShortLinkManager(dao: ShortLinkDao) {

  def create(redirectTo: URL, token: Option[String], expiration: Option[Instant]): Future[ShortLink] =
    dao.create(
      ShortLink(
        id = UUID.randomUUID(),
        token = token.getOrElse(generateShortToken()),
        redirectTo = redirectTo,
        createdDtm = Instant.now(),
        expirationDtm = expiration
      )
    )

  def get(id: UUID): Future[ShortLink] =
    dao.get(id)

  def findByToken(token: String): Future[Option[ShortLink]] =
    dao.findByToken(token)

  def findAll(): Future[Seq[ShortLink]] =
    dao.findAll()

  private def generateShortToken(): String = "placeholder" // TODO

}
