package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class AnalyticsRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  // TODO -
  //  Ideally, these shortlinks would have a randomly generated ID (bigint/UUID)
  //  I bumped into some Slick errors while trying to set up Slick so I kept the
  //  ID as is for now.
  private class AnalyticsTable(tag: Tag) extends Table[Analytics](tag, "analytics") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def token = column[String]("token")
    def hitCount = column[Long]("hit_count")

    def * = (id, token, hitCount) <> ((Analytics.apply _).tupled, Analytics.unapply)
  }

  private val analyticsTable = TableQuery[AnalyticsTable]

  def updateCount(token: String, newCount: Long): Future[Unit] =
    db.run {
      analyticsTable
        .filter(_.token === token)
        .map(_.hitCount)
        .update(newCount)
        .map(_ => ())
    }

  def getOrInitCount(token: String): Future[Long] =
    findCount(token).flatMap {
      case Some(c) => Future(c)
      case _       => initCount(token)
    }

  private def findCount(token: String): Future[Option[Long]] =
    db.run {
      analyticsTable
        .filter(_.token === token)
        .map(_.hitCount)
        .result
        .headOption
    }

  private def initCount(token: String): Future[Long] =
    db.run {
      analyticsTable
        .map(row => (row.token, row.hitCount))
        .returning(analyticsTable.map(_.id))
        .into((row, id) => Analytics(id, row._1, row._2)) += (token, 0)
    }.map(_.hitCount)

}
