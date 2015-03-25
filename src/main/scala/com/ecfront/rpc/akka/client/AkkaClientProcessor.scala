package com.ecfront.rpc.akka.client

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.akka.server.AkkaRequest
import com.ecfront.rpc.process.ClientProcessor
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

/**
 * AKKA 连接处理器
 */
class AkkaClientProcessor extends ClientProcessor {

  private var system: ActorSystem = _

  implicit val timeout = Timeout(20 minutes)

  private var actor: ActorSelection = _

  override protected def init(): Unit = {
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
        |      port = 0
        |    }
        |  }
        |}
      """.stripMargin.format(host)).withFallback(ConfigFactory.load()))
    actor = system.actorSelection("akka.tcp://EZ-RPC-System@" + host + ":" + port + "/user/EZ-RPC")
  }

  override protected[rpc] def processRaw[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => (E) => Unit = null): Unit = {
    val parameter: Map[String, String] = getParameter(path)
    if (fun == null) {
      actor ! AkkaRequest(method, removeParameter(path), parameter, requestBody)
    } else {
      val future = actor ? AkkaRequest(method, removeParameter(path), parameter, requestBody)
      fun(Await.result(future, Duration.Inf).asInstanceOf[E])
    }
  }

  override protected[rpc] def processRaw[E](method: String, path: String, requestBody: Any, responseClass: Class[E]): Future[Option[E]] = {
    val p = Promise[Option[E]]()
    val parameter: Map[String, String] = getParameter(path)
    if (responseClass == null) {
      actor ! AkkaRequest(method, removeParameter(path), parameter, requestBody)
      p.success(null)
    } else {
      val future = actor ? AkkaRequest(method, removeParameter(path), parameter, requestBody)
      p.success(Some(Await.result(future, Duration.Inf).asInstanceOf[E]))
    }
    p.future
  }

  override protected[rpc] def process[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => (Result[E]) => Unit = null): Unit = {
    val parameter: Map[String, String] = getParameter(path)
    if (fun == null) {
      actor ! AkkaRequest(method, removeParameter(path), parameter, requestBody)
    } else {
      val future = actor ? AkkaRequest(method, removeParameter(path), parameter, requestBody)
      fun(Await.result(future, Duration.Inf).asInstanceOf[Result[E]])
    }
  }

  override protected[rpc] def process[E](method: String, path: String, requestBody: Any, responseClass: Class[E]): Future[Option[Result[E]]] = {
    val p = Promise[Option[Result[E]]]()
    val parameter: Map[String, String] = getParameter(path)
    if (responseClass == null) {
      actor ! AkkaRequest(method, removeParameter(path), parameter, requestBody)
      p.success(null)
    } else {
      val future = actor ? AkkaRequest(method, removeParameter(path), parameter, requestBody)
      p.success(Some(Await.result(future, Duration.Inf).asInstanceOf[Result[E]]))
    }
    p.future
  }

  def getParameter[E](path: String): Map[String, String] = {
    val parameter: Map[String, String] = if (path.indexOf("?") != -1) {
      val param = collection.mutable.Map[String, String]()
      path.substring(path.indexOf("?") + 1).split("&").map {
        item =>
          val t = item.split("=")
          if (t.size == 2) {
            param += (t(0) -> t(1))
          } else {
            param += (t(0) -> "")
          }
      }
      param.toMap
    } else {
      Map[String, String]()
    }
    parameter
  }

  def removeParameter(path: String): String = {
    if (path.contains("?")) {
      path.substring(0, path.indexOf("?"))
    } else {
      path
    }
  }

}

