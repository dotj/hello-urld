package com.github.dotj.hellourld

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, PostgresProfile}

//import slick.driver.PostgresDriver.simple._ // deprecated
import slick.jdbc.PostgresProfile.api._

import scala.util.{Failure, Success}

object HelloURLd {

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt("0.0.0.0", 9999).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val connectionUrl = "jdbc:postgresql://localhost/my-db?user=postgres&password=postgres"

      Database.forURL(connectionUrl, driver = "org.postgresql.Driver") withSession { implicit session =>
        val users = TableQuery[Users]

        // SELECT * FROM users
        users.list foreach { row =>
          println("user with id " + row._1 + " has username " + row._2)
        }

        // SELECT * FROM users WHERE username='john'
        users.filter(_.username === "john").list foreach { row =>
          println("user whose username is 'john' has id " + row._1)
        }
      }

      val shortLinkRegistryActor = context.spawn(ShortLinkRegistry(), "ShortLinkRegistryActor")
      context.watch(shortLinkRegistryActor)

      val dc = DatabaseConfig.forConfig[PostgresProfile]("h2_dc")
      val shortLinkDao = new ShortLinkDao(dc.profile)
      val shortLinkManager = new ShortLinkManager(shortLinkDao)

      val routes = new Routes(shortLinkRegistryActor, shortLinkManager)(context.system)
      startHttpServer(routes.allRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloURLdHttpServer")
  }

}
