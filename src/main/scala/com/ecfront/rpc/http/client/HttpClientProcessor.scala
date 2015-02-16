package com.ecfront.rpc.http.client

import com.ecfront.common.JsonHelper
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.RPC.Result.Code
import com.ecfront.rpc.process.ClientProcessor
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse, HttpMethod}
import io.vertx.core.{Handler, Vertx}

/**
 * HTTP 连接处理器
 */
class HttpClientProcessor extends ClientProcessor {

  private val client = Vertx.vertx().createHttpClient(new HttpClientOptions().setKeepAlive(true))

  override protected def init(): Unit = {

  }

  override protected[rpc] def processRaw[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => (E) => Unit): Unit = {
    val body: String = getBody(requestBody)
    client.request(HttpMethod.valueOf(method), s"http://$host:$port$path", new Handler[HttpClientResponse] {
      override def handle(event: HttpClientResponse): Unit = {
        if (fun != null) {
          if (event.statusCode + "" != Code.SUCCESS) {
            logger.error("Server NOT responded.")
          } else {
            event.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                val json = JsonHelper.toJson(data.getString(0, data.length))
                fun(JsonHelper.toObject(json, responseClass))
              }
            })
          }
        }
      }
    }).putHeader("content-type", "application/json; charset=UTF-8").end(body)
  }

  override protected[rpc] def process[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => (Result[E]) => Unit): Unit = {
    val body: String = getBody(requestBody)
    client.request(HttpMethod.valueOf(method), s"http://$host:$port$path", new Handler[HttpClientResponse] {
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
                  fun(Result.success(JsonHelper.toObject(json.get(Result.BODY), responseClass)))
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

  private def getBody[E](requestBody: Any): String = {
    val body = requestBody match {
      case str: String => str
      case _ => JsonHelper.toJsonString(requestBody)
    }
    body
  }

}

