package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

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

    def * = (id, redirectToUrl, token) <> ((ShortLink.apply _).tupled, ShortLink.unapply)
  }

  private val shortLinkTable = TableQuery[ShortLinkTable]

  def create(redirectToUrl: String, token: String): Future[ShortLink] = db.run {
    (shortLinkTable.map(l => (l.redirectToUrl, l.token))
      returning shortLinkTable.map(_.id)
      into ((link, id) => ShortLink(id, link._1, link._2))) += (redirectToUrl, token)
  }

  def list(): Future[Seq[ShortLink]] = db.run {
    shortLinkTable.result
  }
}
