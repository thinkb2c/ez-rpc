package com.ecfront.rpc.http.client

import com.ecfront.common.JsonHelper
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.RPC.Result.Code
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse, HttpMethod}
import io.vertx.core.{Handler, Vertx}

/**
 * HTTP 客户端处理器<br/>
 */
object HttpClientProcessor extends LazyLogging {

  private val client = Vertx.vertx().createHttpClient(new HttpClientOptions().setKeepAlive(true))

  private[rpc] def process[E](method: HttpMethod, url: String, requestBody: Any, responseBodyClass: Class[E], fun: => Result[E] => Unit): Unit = {
    val body = requestBody match {
      case str: String => str
      case _ => JsonHelper.toJsonString(requestBody)
    }
    client.request(method, url, new Handler[HttpClientResponse] {
      override def handle(event: HttpClientResponse): Unit = {
        if (fun != null) {
          if (event.statusCode + "" != Code.SUCCESS) {
            fun(Result.serverUnavailable[E]("Server NOT responded."))
          } else {
            event.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                val json = JsonHelper.toJson(data.getString(0, data.length))
                val code = json.get(Result.CODE).asText()
                if (code == Code.SUCCESS) {
                  fun(Result.success(JsonHelper.toObject(json.get(Result.BODY), responseBodyClass)))
                } else {
                  fun(Result.customFail(code, json.get(Result.MESSAGE).asText()))
                }
              }
            })
          }
        }
      }
    }).putHeader("content-type", "application/json; charset=UTF-8").end(body)
  }

}

