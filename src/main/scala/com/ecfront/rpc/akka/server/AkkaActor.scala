package com.ecfront.rpc.akka.server

import akka.actor.{Actor, Props}
import com.ecfront.rpc.Router
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * AKKA Actor
 */
class AkkaActor(router: Router) extends Actor with LazyLogging {

  def receive = {
    case AkkaRequest(method, path, parameter, body) =>
      val urlParameter = collection.mutable.Map[String, String]()
      parameter.foreach {
        item =>
          urlParameter += (item._1 -> item._2)
      }
      val (preResult, fun, postFun) = router.getFunction(method, path, urlParameter)
      if (preResult) {
        sender() ! postFun(fun.execute(urlParameter.toMap, body, preResult.body))
      } else {
        sender() ! preResult
      }
  }

}

object AkkaActor {
  private[rpc] def props(router: Router): Props = Props(new AkkaActor(router))
}


