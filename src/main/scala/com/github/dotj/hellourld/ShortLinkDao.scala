package com.github.dotj.hellourld

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{JdbcProfile, PostgresProfile}
import slick.lifted.Tag
import slick.model.Table

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

//import slick.driver.MySQLDriver.simple._
//import slick.jdbc.MySQLProfile.api._

import slick.jdbc.PostgresProfile.Table
import slick.jdbc.PostgresProfile.columnTypes._


class ShortLinkTable(tag: Tag) extends Table[(UUID, String, String, Instant, Instant)](tag, "shortlink") {
  def id = column[UUID]("id", O.PrimaryKey)
  def token = column[String]("token")
  def redirectTo = column[String]("redirect_to_url")
  def createdDtm = column[Instant]("created_dtm")
  def expirationDtm = column[Instant]("expiration_dtm")
  def * = (id, token, redirectTo, createdDtm, expirationDtm)
}

class ShortLinkDao(db: PostgresProfile) {

  def create(shortLink: ShortLink): Future[ShortLink] = {
    val query:
  }

  def get(id: UUID): Future[ShortLink] = ???
  def findByToken(token: String): Future[Option[ShortLink]] = ???
  def findAll(): Future[Seq[ShortLink]] = ???

}
