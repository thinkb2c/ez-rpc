package com.ecfront.rpc.http.server

import java.io.File
import java.util.UUID

import com.ecfront.common.JsonHelper
import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core._
import io.vertx.core.http.{HttpMethod, HttpServerResponse}
import io.vertx.ext.apex.core.{Cookie, RoutingContext}

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
 * HTTP 服务端处理器<br/>
 */
object HttpServerProcessor extends LazyLogging {

  private var vertx: Vertx = _
  private var baseUploadPath: String = _

  private[rpc] def init(vert: Vertx, path: String) {
    vertx = vert
    baseUploadPath = path
    if (!baseUploadPath.endsWith(File.separator)) {
      baseUploadPath += File.separator
    }
  }

  private[rpc] def uploadProcess(uploadPath: Option[String], allowType: Option[List[String]], fun: => (MultiMap, Set[String], Set[Cookie]) => Result[Any]) = {
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
  }

  private[rpc] def normalProcess(method: HttpMethod, uri: String, fun: => (MultiMap, Set[Cookie]) => Result[Any]) = {
    new Handler[RoutingContext] {
      override def handle(event: RoutingContext): Unit = {
        vertx.executeBlocking(new Handler[Future[Result[Any]]] {
          override def handle(future: Future[Result[Any]]): Unit = {
            future.complete(fun(event.request().params(), event.cookies().toSet))
          }
        }, new Handler[AsyncResult[Result[Any]]] {
          override def handle(result: AsyncResult[Result[Any]]): Unit = {
            returnJson(result.result(), event.response())
          }
        })
      }
    }
  }

  private[rpc] def submitProcess[E](method: HttpMethod, uri: String, requestClass: Class[E], fun: => (MultiMap, E, Set[Cookie]) => Result[Any]) = {
    new Handler[RoutingContext] {
      override def handle(event: RoutingContext): Unit = {
        vertx.executeBlocking(new Handler[Future[Result[Any]]] {
          override def handle(future: Future[Result[Any]]): Unit = {
            future.complete(fun(event.request().params(), JsonHelper.toObject(event.getBodyAsString, requestClass), event.cookies().toSet))
          }
        }, new Handler[AsyncResult[Result[Any]]] {
          override def handle(result: AsyncResult[Result[Any]]): Unit = {
            returnJson(result.result(), event.response())
          }
        })
      }
    }
  }

  private def returnJson(result: AnyRef, response: HttpServerResponse) {
    response.setStatusCode(200).putHeader("Content-Type", "application/json; charset=UTF-8")
      .putHeader("Cache-Control", "no-cache")
      /* .putHeader("Access-Control-Allow-Origin", "*")
       .putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
       .putHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, X-authentication, X-client")*/
      .end(JsonHelper.toJsonString(result))
  }
}
