package com.ecfront.rpc.http.client

import java.net.URL

import com.ecfront.common.{JsonHelper, Resp, StandardCode}
import com.ecfront.rpc.process.ClientProcessor
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse, HttpMethod}
import io.vertx.core.{Handler, Vertx}

import scala.concurrent.{Future, Promise}
import scala.xml._

/**
 * HTTP 连接处理器
 */
class HttpClientProcessor extends ClientProcessor {

  private val client = Vertx.vertx().createHttpClient(new HttpClientOptions().setKeepAlive(true))

  override protected def init(): Unit = {

  }

  private def getUrlInfo(path: String): (String, Int, String) = {
    var tHost = host
    var tPort = port
    var tPath = path
    if (path.toLowerCase.startsWith("http")) {
      val url = new URL(path)
      tHost = url.getHost
      tPort = url.getPort
      tPath = url.getPath
    }
    (tHost, tPort, tPath)
  }

  override protected[rpc] def processRaw[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => (E) => Unit): Unit = {
    val jsonType = responseClass != classOf[scala.xml.Node]
    val body: String = getBody(requestBody, if (jsonType) "json" else "xml")
    val (tHost, tPort, tPath) = getUrlInfo(path)
    client.request(HttpMethod.valueOf(method), tPort, tHost, tPath, new Handler[HttpClientResponse] {
      override def handle(response: HttpClientResponse): Unit = {
        if (fun != null) {
          if (response.statusCode + "" != StandardCode.SUCCESS) {
            logger.error("Server NOT responded.")
          } else {
            response.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                if (jsonType) {
                  val json = JsonHelper.toJson(data.getString(0, data.length))
                  fun(JsonHelper.toObject(json, responseClass))
                } else {
                  fun(XML.loadString(data.getString(0, data.length)).asInstanceOf[E])
                }
              }
            })
          }
        }
      }
    }).putHeader("content-type", if (jsonType) "application/json; charset=UTF-8" else "application/xml; charset=UTF-8").end(body)
  }

  override protected[rpc] def processRaw[E](method: String, path: String, requestBody: Any, responseClass: Class[E]): Future[Option[E]] = {
    val jsonType = responseClass != classOf[scala.xml.Node]
    val p = Promise[Option[E]]()
    val body: String = getBody(requestBody, if (jsonType) "json" else "xml")
    val (tHost, tPort, tPath) = getUrlInfo(path)
    client.request(HttpMethod.valueOf(method), tPort, tHost, tPath, new Handler[HttpClientResponse] {
      override def handle(response: HttpClientResponse): Unit = {
        if (responseClass != null) {
          if (response.statusCode + "" != StandardCode.SUCCESS) {
            logger.error("Server NOT responded.")
            p.failure(new Exception(response.statusMessage()))
          } else {
            response.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                if (jsonType) {
                  val json = JsonHelper.toJson(data.getString(0, data.length))
                  p.success(Some(JsonHelper.toObject(json, responseClass)))
                } else {
                  p.success(Some(XML.loadString(data.getString(0, data.length)).asInstanceOf[E]))
                }
              }
            })
          }
        } else {
          p.success(null)
        }
      }
    }).putHeader("content-type", if (jsonType) "application/json; charset=UTF-8" else "application/xml; charset=UTF-8").end(body)
    p.future
  }

  override protected[rpc] def process[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => (Resp[E]) => Unit): Unit = {
    val body: String = getBody(requestBody, "json")
    val (tHost, tPort, tPath) = getUrlInfo(path)
    client.request(HttpMethod.valueOf(method), tPort, tHost, tPath, new Handler[HttpClientResponse] {
      override def handle(response: HttpClientResponse): Unit = {
        if (fun != null) {
          if (response.statusCode + "" != StandardCode.SUCCESS) {
            fun(Resp.serverUnavailable[E]("Server NOT responded."))
          } else {
            response.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                val json = JsonHelper.toJson(data.getString(0, data.length))
                val code = json.get(Resp.CODE).asText()
                if (code == StandardCode.SUCCESS) {
                  fun(Resp.success(JsonHelper.toObject(json.get(Resp.BODY), responseClass)))
                } else {
                  fun(Resp.fail(code, json.get(Resp.MESSAGE).asText()))
                }
              }
            })
          }
        }
      }
    }).putHeader("content-type", "application/json; charset=UTF-8").end(body)
  }

  override protected[rpc] def process[E](method: String, path: String, requestBody: Any, responseClass: Class[E]): Future[Option[Resp[E]]] = {
    val body: String = getBody(requestBody, "json")
    val p = Promise[Option[Resp[E]]]()
    val (tHost, tPort, tPath) = getUrlInfo(path)
    client.request(HttpMethod.valueOf(method), tPort, tHost, tPath, new Handler[HttpClientResponse] {
      override def handle(response: HttpClientResponse): Unit = {
        if (responseClass != null) {
          if (response.statusCode + "" != StandardCode.SUCCESS) {
            p.success(Some(Resp.serverUnavailable[E]("Server NOT responded.")))
          } else {
            response.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                val json = JsonHelper.toJson(data.getString(0, data.length))
                val code = json.get(Resp.CODE).asText()
                if (code == StandardCode.SUCCESS) {
                  p.success(Some(Resp.success(JsonHelper.toObject(json.get(Resp.BODY), responseClass))))
                } else {
                  p.success(Some(Resp.fail(code, json.get(Resp.MESSAGE).asText())))
                }
              }
            })
          }
        } else {
          p.success(null)
        }
      }
    }).putHeader("content-type", "application/json; charset=UTF-8").end(body)
    p.future
  }

  private def getBody[E](requestBody: Any, contentType: String = "json"): String = {
    requestBody match {
      case b: String => b
      case b if contentType == "json" => JsonHelper.toJsonString(b)
      case b if contentType == "xml" => requestBody.toString
    }
  }

}

