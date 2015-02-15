package com.ecfront.rpc.akka.client

import akka.actor.ActorSelection
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.akka.server.AkkaRequest
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * AKKA 客户端<br/>
 */
class AkkaClient extends LazyLogging {

  private var actor: ActorSelection = _

  private[rpc] def startup(port: Int, host: String) = {
    actor = AkkaClientProcessor.init(port, host)
    this
  }

  def process[E](request: AkkaRequest, fun: => Result[E] => Unit = null) {
    AkkaClientProcessor.process(actor, request, fun)
  }


}

