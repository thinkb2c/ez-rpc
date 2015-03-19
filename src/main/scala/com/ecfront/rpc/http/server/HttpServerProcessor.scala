package com.ecfront.rpc.http.server

import java.util.concurrent.CountDownLatch

import com.ecfront.common.JsonHelper
import com.ecfront.rpc.Fun
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.process.ServerProcessor
import io.vertx.core._
import io.vertx.core.buffer.Buffer
import io.vertx.core.http._

import scala.collection.JavaConversions._

/**
 * HTTP服务处理器
 */
class HttpServerProcessor extends ServerProcessor {

  private val vertx = Vertx.vertx()
  private var server: HttpServer = _

  override protected def init(): Unit = {
    val latch = new CountDownLatch(1)
    server = vertx.createHttpServer(new HttpServerOptions().setHost(host).setPort(port).setCompressionSupported(true))
      .requestHandler(new Handler[HttpServerRequest] {
      override def handle(request: HttpServerRequest): Unit = {
        val (fun, urlParameter) = router.getFunction(request.method().name(), request.path())
        val contentType = if (request.headers().contains("content-type"))
          request.headers().get("content-type").toLowerCase
        else "application/json; charset=UTF-8"
        if (fun != null) {
          request.params().entries().foreach {
            item =>
              urlParameter += (item.getKey -> item.getValue)
          }
          if (request.method().name() == "POST" || request.method().name() == "PUT") {
            request.bodyHandler(new Handler[Buffer] {
              override def handle(data: Buffer): Unit = {
                val body = contentType match {
                  case t if t.contains("json") => JsonHelper.toObject(data.getString(0, data.length), fun.requestClass)
                  case t if t.contains("xml") => scala.xml.XML.loadString(data.getString(0, data.length))
                  case _ => logger.error("Not support content type:" + contentType)
                }
                execute(urlParameter.toMap, body, fun, request.response(), contentType)
              }
            })
          } else {
            execute(urlParameter.toMap, null, fun, request.response(), contentType)
          }
        } else {
          logger.warn("Not implemented: [ %s ] %s".format(request.method().name(), request.path()))
          returnContent(Result.badRequest("[ %s ] %s".format(request.method().name(), request.path())), request.response(), contentType)
        }
      }
    }).listen(new Handler[AsyncResult[HttpServer]] {
      override def handle(event: AsyncResult[HttpServer]): Unit = {
        if (event.succeeded()) {
          latch.countDown()
        } else {
          logger.error("Startup fail.", event.cause())
        }
      }
    })
    latch.await()
  }

  override private[rpc] def destroy(): Unit = {
    val latch = new CountDownLatch(1)
    server.close(new Handler[AsyncResult[Void]] {
      override def handle(event: AsyncResult[Void]): Unit = {
        if (event.succeeded()) {
          latch.countDown()
        } else {
          logger.error("Shutdown failed.", event.cause())
        }
      }
    })
    latch.await()
  }

  private def execute(parameter: Map[String, String], body: Any, fun: Fun[_], response: HttpServerResponse, contentType: String) {
    vertx.executeBlocking(new Handler[Future[Any]] {
      override def handle(future: Future[Any]): Unit = {
        future.complete(fun.execute(parameter, body))
      }
    }, new Handler[AsyncResult[Any]] {
      override def handle(result: AsyncResult[Any]): Unit = {
        returnContent(result.result(), response, contentType)
      }
    })
  }

  private def returnContent(result: Any, response: HttpServerResponse, contentType: String) {
    val body = contentType match {
      case t if t.contains("json") => JsonHelper.toJsonString(result)
      case t if t.contains("xml") => result.toString
      case _ =>
        logger.error("Not support content type:" + contentType)
        ""
    }
    response.setStatusCode(200).putHeader("Content-Type", contentType)
      .putHeader("Cache-Control", "no-cache")
      .putHeader("Access-Control-Allow-Origin", "*")
      .putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
      .putHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, X-authentication, X-client")
      .end(body)
  }

  /*private[rpc] def uploadProcess(uploadPath: Option[String], allowType: Option[List[String]], fun: => (MultiMap, Set[String], Set[Cookie]) => Result[Any]) = {
    new Handler[RoutingContext] {
      override def handle(event: RoutingContext): Unit = {
        val files = ArrayBuffer[String]()
        event.fileUploads().foreach {
          upload =>
            if (allowType == null || allowType.contains(upload.contentType().toLowerCase)) {
              var fileName: String = baseUploadPath
              if (uploadPath != null && "" != uploadPath.get.trim) {
                fileName += uploadPath.get.trim
                if (!fileName.endsWith(File.separator)) {
                  fileName += File.separator
                }
              }
              if (-1 != upload.fileName.lastIndexOf(".")) {
                fileName += upload.fileName.substring(0, upload.fileName.lastIndexOf(".")) + "_" + UUID.randomUUID + "." + upload.fileName.substring(upload.fileName.lastIndexOf(".") + 1)
              } else {
                fileName += upload.fileName + "_" + UUID.randomUUID
              }
              //TODO
              //upload.streamToFileSystem(fileName)
              files += fileName
            } else {
              logger.warn("Upload type [%s] NOT allowed.".format(upload.contentType()))
            }
        }
        vertx.executeBlocking(new Handler[Future[Result[Any]]] {
          override def handle(future: Future[Result[Any]]): Unit = {
            future.complete(fun(event.request().params(), files.toSet, event.cookies().toSet))
          }
        }, new Handler[AsyncResult[Result[Any]]] {
          override def handle(result: AsyncResult[Result[Any]]): Unit = {
            returnJson(result.result(), event.response())
          }
        })
      }
    }
  }*/

}
