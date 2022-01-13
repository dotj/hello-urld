package com.github.dotj.hellourld

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

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
      val shortLinkRegistryActor = context.spawn(ShortLinkRegistry(), "ShortLinkRegistryActor")
      context.watch(shortLinkRegistryActor)

      val shortLinkManager = new ShortLinkManager(shortLinkRegistryActor)(context.system)
      val routes = new Routes(shortLinkManager)
      startHttpServer(routes.allRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloURLdHttpServer")
  }

}
