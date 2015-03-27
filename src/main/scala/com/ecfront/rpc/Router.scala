package com.ecfront.rpc

import java.util.regex.Pattern

import com.ecfront.common.Resp
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.mutable

/**
 * 路由操作对象
 * @param requestClass 请求对象的类型
 * @param fun 业务方法
 */
case class Fun[E](requestClass: Class[E], private val fun: (Map[String, String], E, Any) => Any) {
  private[rpc] def execute(parameters: Map[String, String], body: Any, inject: Any): Any = {
    fun(parameters, body.asInstanceOf[E], inject)
  }
}

/**
 * 路由表
 */
private[rpc] class Router extends LazyLogging {

  //业务方法容器，非正则
  private val funContainer = collection.mutable.Map[String, collection.mutable.Map[String, (Fun[_], Server)]]()
  funContainer += ("POST" -> collection.mutable.Map[String, (Fun[_], Server)]())
  funContainer += ("GET" -> collection.mutable.Map[String, (Fun[_], Server)]())
  funContainer += ("DELETE" -> collection.mutable.Map[String, (Fun[_], Server)]())
  funContainer += ("PUT" -> collection.mutable.Map[String, (Fun[_], Server)]())
  //业务方法容器，正则
  private val funContainerR = collection.mutable.Map[String, mutable.Buffer[((Pattern, Seq[String]), (Fun[_], Server))]]()
  funContainerR += ("POST" -> mutable.Buffer[((Pattern, Seq[String]), (Fun[_], Server))]())
  funContainerR += ("GET" -> mutable.Buffer[((Pattern, Seq[String]), (Fun[_], Server))]())
  funContainerR += ("DELETE" -> mutable.Buffer[((Pattern, Seq[String]), (Fun[_], Server))]())
  funContainerR += ("PUT" -> mutable.Buffer[((Pattern, Seq[String]), (Fun[_], Server))]())
  //正则转换前后的映射，如 ^/base/test2/(?<id>[^/]+)/$ -> /base/test2/:id/
  private val containerOriginalR = collection.mutable.Map[String, String]()


  /**
   * 获取对应的路由信息，先按非正则匹配，匹配再从正则容器中查找
   * @param method 资源操作方式
   * @param path 资源路径
   */
  private[rpc] def getFunction(method: String, path: String, parameters: collection.mutable.Map[String, String]): (Resp[Any], Fun[_], Any => Any) = {
    var urlTemplate: String = path
    var fun: Fun[_] = null
    var server: Server = null
    val r = funContainer.get(method.toUpperCase).get.get(path).orNull
    if (r == null) {
      funContainerR.get(method).get.foreach {
        item =>
          val matcher = item._1._1.matcher(path)
          if (matcher.matches()) {
            urlTemplate = containerOriginalR(item._1._1.pattern())
            fun = item._2._1
            server = item._2._2
            item._1._2.foreach(name => parameters += (name -> matcher.group(name)))
          }
      }
    } else {
      fun = r._1
      server = r._2
    }
    if (server != null) {
      val result = server.preExecute(method, urlTemplate, parameters.toMap)
      if (result) {
        (result, fun, server.postExecute)
      } else {
        (Resp.fail(result.code, result.message), null, null)
      }
    } else {
      (Resp.notImplemented("[ %s ] %s".format(method, path)), null, null)
    }
  }

  /**
   * 注册路由规则
   * @param method 资源操作方式
   * @param path 资源路径
   * @param requestClass 请求对象的类型
   * @param fun 业务方法
   * @param server server对象
   */
  private[rpc] def add[E](method: String, path: String, requestClass: Class[E], fun: => (Map[String, String], E, Any) => Any, server: Server) {
    val nPath = server.formatUrl(path)
    logger.info(s"Register [${server.getChannel}] method [$method] path : $nPath.")
    if (nPath.contains(":")) {
      //regular
      val r = Router.getRegex(nPath)
      containerOriginalR += r._1.pattern() -> nPath
      funContainerR.get(method).get += (r ->(Fun[E](requestClass, fun), server))
    } else {
      funContainer.get(method).get += (nPath ->(Fun[E](requestClass, fun), server))
    }
  }
}

object Router {

  private def getRegex(path: String): (Pattern, Seq[String]) = {
    var pathR = path
    var named = mutable.Buffer[String]()
    """:\w+""".r.findAllMatchIn(path).foreach {
      m =>
        val name = m.group(0).substring(1)
        pathR = pathR.replaceAll(m.group(0), """(?<""" + name + """>[^/]+)""")
        named += name
    }
    (Pattern.compile("^" + pathR + "$"), named)
  }

}
