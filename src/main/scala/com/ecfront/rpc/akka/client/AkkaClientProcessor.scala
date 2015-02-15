package com.ecfront.rpc.akka.client

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.akka.server.AkkaRequest
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * AKKA 客户端处理器<br/>
 */
object AkkaClientProcessor extends LazyLogging {

  private val system: ActorSystem = ActorSystem("EZ-RPC-System", ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    provider = "akka.remote.RemoteActorRefProvider"
      |  }
      |}
    """.stripMargin).withFallback(ConfigFactory.load()))
  implicit val timeout = Timeout(20 minutes)

  private[rpc] def init(port: Int, host: String): ActorSelection = {
    system.actorSelection("akka.tcp://EZ-RPC-System@" + host + ":" + port + "/user/EZ-RPC")
  }

  def process[E](actor: ActorSelection, request: AkkaRequest, fun: => Result[E] => Unit): Unit = {
    if (fun == null) {
      actor ! request
    } else {
      val future = (actor ? request).mapTo[Result[E]]
      fun(Await.result(future, Duration.Inf))
    }
  }
}

