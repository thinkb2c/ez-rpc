package com.ecfront.rpc.akka.server

import akka.actor.{Actor, ActorSystem, Props}
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.RPC.Result.Code
import com.typesafe.config.ConfigFactory

/**
 * AKKA 服务端处理器<br/>
 */
object AkkaServerProcessor {

  private var system: ActorSystem = _

  private[rpc] def init(port: Int, host: String) {
    system = ActorSystem("EZ-RPC-System", ConfigFactory.parseString(
      """
        |akka {
        |  actor {
        |    provider = "akka.remote.RemoteActorRefProvider"
        |  }
        |  remote {
        |    enabled-transports = ["akka.remote.netty.tcp"]
        |    netty.tcp {
        |      hostname = "%s"
        |      port = %s
        |    }
        |  }
        |}
      """.stripMargin.format(host, port)).withFallback(ConfigFactory.load()))
  }

  private[rpc] def process(fun: => AkkaRequest => Result[Any]): Unit = {
    system.actorOf(props(fun), name = "EZ-RPC")
  }

  private def props(fun: => AkkaRequest => Result[Any]): Props = Props(new AkkaServerProcessor(fun))

}

class AkkaServerProcessor(fun: => AkkaRequest => Result[Any]) extends Actor {
  def receive = {
    case AkkaRequest(action, body) =>
      val tmpSender = sender()
      if (null == action || action.trim.isEmpty) {
        tmpSender ! Result.customFail(Code.BAD_REQUEST, "Request parameter must contain [action].")
      } else if (fun != null) {
        tmpSender ! fun(AkkaRequest(action, body))
      }
  }
}
