package com.ecfront.rpc.akka.server

import akka.actor.ActorSystem
import com.ecfront.rpc.process.ServerProcessor
import com.typesafe.config.ConfigFactory

/**
 * AKKA 服务处理器
 */
class AkkaServerProcessor extends ServerProcessor {

  private var system: ActorSystem = _

  override def init() {
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
    system.actorOf(AkkaActor.props(router), name = "EZ-RPC")
  }

  override private[rpc] def destroy(): Unit = {
    system.shutdown()
  }

}

