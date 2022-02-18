package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("user")
    def hashedPassword = column[String]("hashed_password")

    def * = (id, name, hashedPassword) <> ((User.apply _).tupled, User.unapply)
  }

  private val userTable = TableQuery[UserTable]

  def createUser(name: String, password: String): Future[User] = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashedPassword: String = digest.digest(password.getBytes(StandardCharsets.UTF_8)).mkString

    db.run {
      userTable
        .map(row => (row.name, row.hashedPassword))
        .returning(userTable.map(_.id))
        .into((row, id) => User(id, row._1, row._2)) += (name, hashedPassword)
    }
  }

}
