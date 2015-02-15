package com.ecfront.rpc.http.client

import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.http.HttpMethod

/**
 * HTTP 客户端<br/>
 */
class HttpClient extends LazyLogging {

  def getAsync[E](url: String, responseBodyClass: Class[E] = null, fun: => Result[E] => Unit = null): Unit = {
    HttpClientProcessor.process[E](HttpMethod.GET, url, "", responseBodyClass, fun)
  }

  def deleteAsync[E](url: String, responseBodyClass: Class[E] = null, fun: => Result[E] => Unit = null): Unit = {
    HttpClientProcessor.process[E](HttpMethod.DELETE, url, "", responseBodyClass, fun)
  }

  def postAsync[E](url: String, data: Any, responseBodyClass: Class[E] = null, fun: => Result[E] => Unit = null): Unit = {
    HttpClientProcessor.process[E](HttpMethod.POST, url, data, responseBodyClass, fun)
  }

  def putAsync[E](url: String, data: Any, responseBodyClass: Class[E] = null, fun: => Result[E] => Unit = null): Unit = {
    HttpClientProcessor.process[E](HttpMethod.PUT, url, data, responseBodyClass, fun)
  }


}

