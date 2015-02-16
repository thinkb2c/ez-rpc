package com.ecfront.rpc.akka.server

import akka.actor.{Actor, Props}
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.Router
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * AKKA Actor
 */
class AkkaActor(router: Router) extends Actor with LazyLogging {

  def receive = {
    case AkkaRequest(method, path, parameter, body) =>
      val (fun, urlParameter) = router.getFunction(method, path)
      if (fun != null) {
        parameter.foreach {
          item =>
            urlParameter += (item._1 -> item._2)
        }
        sender() ! fun.execute(urlParameter.toMap, body)
      } else {
        logger.warn("Not implemented: [ %s ] %s".format(method, path))
        sender() ! Result.notImplemented("[ %s ] %s".format(method, path))
      }
  }

}

object AkkaActor {
  private[rpc] def props(router: Router): Props = Props(new AkkaActor(router))
}


