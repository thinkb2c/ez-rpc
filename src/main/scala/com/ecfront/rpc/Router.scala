package com.ecfront.rpc

import java.util.regex.Pattern

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.mutable

/**
 * 路由操作对象
 * @param requestClass 请求对象的类型
 * @param fun 业务方法
 */
case class Fun[E](requestClass: Class[E], private val fun: (Map[String, String], E) => Any) {
  private[rpc] def execute(parameters: Map[String, String], body: Any): Any = {
    fun(parameters, body.asInstanceOf[E])
  }
}

/**
 * 路由表
 */
private[rpc] class Router extends LazyLogging {

  //业务方法容器，非正则
  private val funContainer = collection.mutable.Map[String, collection.mutable.Map[String, Fun[_]]]()
  funContainer += ("POST" -> collection.mutable.Map[String, Fun[_]]())
  funContainer += ("GET" -> collection.mutable.Map[String, Fun[_]]())
  funContainer += ("DELETE" -> collection.mutable.Map[String, Fun[_]]())
  funContainer += ("PUT" -> collection.mutable.Map[String, Fun[_]]())
  //业务方法容器，正则
  private val funContainerR = collection.mutable.Map[String, mutable.Buffer[((Pattern, Seq[String]), Fun[_])]]()
  funContainerR += ("POST" -> mutable.Buffer[((Pattern, Seq[String]), Fun[_])]())
  funContainerR += ("GET" -> mutable.Buffer[((Pattern, Seq[String]), Fun[_])]())
  funContainerR += ("DELETE" -> mutable.Buffer[((Pattern, Seq[String]), Fun[_])]())
  funContainerR += ("PUT" -> mutable.Buffer[((Pattern, Seq[String]), Fun[_])]())


  /**
   * 获取对应的路由信息，先按非正则匹配，匹配再从正则容器中查找
   * @param method 资源操作方式
   * @param path 资源路径
   */
  private[rpc] def getFunction(method: String, path: String): (Fun[_], collection.mutable.Map[String, String]) = {
    val parameters = collection.mutable.Map[String, String]()
    var res = funContainer.get(method.toUpperCase).get.get(path).orNull
    if (res == null) {
      funContainerR.get(method).get.foreach {
        item =>
          val matcher = item._1._1.matcher(path)
          if (matcher.matches()) {
            res = item._2
            item._1._2.foreach(name => parameters += (name -> matcher.group(name)))
          }
      }
    }
    if (res == null) {
      (null, parameters)
    } else {
      (res, parameters)
    }
  }

  /**
   * 注册路由规则
   * @param method 资源操作方式
   * @param path 资源路径
   * @param requestClass 请求对象的类型
   * @param fun 业务方法
   */
  private[rpc] def add[E](method: String, path: String, requestClass: Class[E], fun: => (Map[String, String], E) => Any) {
    logger.info("Register method [%s] path : %s".format(method, path))
    if (path.contains(":")) {
      //regular
      funContainerR.get(method).get += (Router.getRegex(path) -> Fun[E](requestClass, fun))
    } else {
      funContainer.get(method).get += (path -> Fun[E](requestClass, fun))
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
