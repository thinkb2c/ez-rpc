
package com.ecfront.rpc.http.server

import java.util.regex.Pattern

import scala.collection.mutable

/**
 * 业务方法容器类
 */
private[rpc] object HttpFunctionContainer {

  //业务方法容器，非正则
  private val funContainer = collection.mutable.Map[String, collection.mutable.Map[String, HttpFun[_]]]()
  funContainer += ("POST" -> collection.mutable.Map[String, HttpFun[_]]())
  funContainer += ("GET" -> collection.mutable.Map[String, HttpFun[_]]())
  funContainer += ("DELETE" -> collection.mutable.Map[String, HttpFun[_]]())
  funContainer += ("PUT" -> collection.mutable.Map[String, HttpFun[_]]())
  //业务方法容器，正则
  private val funContainerR = collection.mutable.Map[String, mutable.Buffer[((Pattern, Seq[String]), HttpFun[_])]]()
  funContainerR += ("POST" -> mutable.Buffer[((Pattern, Seq[String]), HttpFun[_])]())
  funContainerR += ("GET" -> mutable.Buffer[((Pattern, Seq[String]), HttpFun[_])]())
  funContainerR += ("DELETE" -> mutable.Buffer[((Pattern, Seq[String]), HttpFun[_])]())
  funContainerR += ("PUT" -> mutable.Buffer[((Pattern, Seq[String]), HttpFun[_])]())

  /**
   * 获取对应的方法，先按非正则匹配，匹配再从正则容器中查找
   * @param method 方法
   * @param path path
   * @return
   */
  private[http] def getFunction(method: String, path: String): (HttpFun[_], collection.mutable.Map[String, String]) = {
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
    (res, parameters)
  }


  private[http] def add(method: String, path: String, function: HttpFun[_]) {
    if (path.contains(":")) {
      //regular
      funContainerR.get(method).get += ((getRegex(path), function))
    } else {
      funContainer.get(method).get += (path -> function)
    }
  }


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











