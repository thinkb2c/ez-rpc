package com.ecfront.rpc.http.client

import com.ecfront.common.JsonHelper
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse, HttpMethod}
import io.vertx.core.{Handler, Vertx}

/**
 * TODO HTTP 客户端<br/>
 * 支持标准的基于Json的Restful风格，返回结果为统一的HttpResult对象
 */
class HttpClient extends LazyLogging {

  val client = Vertx.vertx().createHttpClient(new HttpClientOptions().setKeepAlive(true))

  private def execute(method: HttpMethod, url: String, body: Any): Unit = {
    client.request(method, url, new Handler[HttpClientResponse] {
      override def handle(event: HttpClientResponse): Unit = {
      }
    }).putHeader("content-type", "application/json; charset=UTF-8").end(JsonHelper.toJsonString(body))
  }

}

