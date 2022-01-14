package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class ShortLinkRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class ShortLinkTable(tag: Tag) extends Table[ShortLink](tag, "short_link") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def redirectToUrl = column[String]("redirect_to_url")
    def token = column[String]("token")
    def expiration = column[Option[LocalDate]]("expiration_date")
    def expired = column[Boolean]("expired")

    def * = (id, redirectToUrl, token, expiration, expired) <> ((ShortLink.apply _).tupled, ShortLink.unapply)
  }

  private val shortLinkTable = TableQuery[ShortLinkTable]

  def create(redirectToUrl: String, token: String, expirationDate: Option[LocalDate]): Future[ShortLink] = db.run {
    shortLinkTable
      .map(sl => (sl.redirectToUrl, sl.token, sl.expiration))
      .returning(shortLinkTable.map(_.id))
      .into((sl, id) => ShortLink(id, sl._1, sl._2, sl._3)) += (redirectToUrl, token, expirationDate)
  }

  def getAll(): Future[Seq[ShortLink]] = db.run {
    shortLinkTable
      .filterNot(_.expired)
      .result
  }

  def findById(id: Long): Future[Option[ShortLink]] = db.run {
    shortLinkTable
      .filter(_.id === id)
      .filterNot(_.expired)
      .result
      .headOption
  }

  def findByToken(token: String): Future[Option[ShortLink]] =
    db.run {
      shortLinkTable
        .filter(_.token === token)
        .filterNot(_.expired)
        .result
        .headOption
    }

  def update(token: String, newRedirectToUrl: String): Future[Unit] =
    db.run {
      shortLinkTable
        .filter(_.token === token)
        .map(_.redirectToUrl)
        .update(newRedirectToUrl)
        .map(_ => ())
    }

  def deprecate(id: Long): Future[Unit] =
    db.run {
      shortLinkTable
        .filter(_.id === id)
        .map(_.expired)
        .update(false)
        .map(_ => ())
    }

  def delete(id: Long): Future[Unit] =
    db.run {
      shortLinkTable
        .filter(_.id === id)
        .delete
        .map(_ => ())
    }

}
