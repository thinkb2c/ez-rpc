package com.ecfront.rpc.http.server

import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.http.{HttpMethod, HttpServerOptions, HttpServerRequest}
import io.vertx.core.{Handler, MultiMap, Vertx}
import io.vertx.ext.apex.addons.CorsHandler
import io.vertx.ext.apex.core.{BodyHandler, Cookie, CookieHandler, Router}

/**
 * HTTP服务器<br/>
 * 支持标准的基于Json的Restful风格，返回结果为统一的Result对象
 */
class HttpServer extends LazyLogging {

  val vertx = Vertx.vertx()
  val router = Router.router(vertx)
  router.route().handler(BodyHandler.bodyHandler())
  router.route().handler(CookieHandler.cookieHandler())
  router.route().handler(CorsHandler.cors("*"))

  private[rpc] def startup(port: Int, host: String, baseUploadPath: String) = {
    logger.info("Http Service starting")
    HttpServerProcessor.init(vertx, baseUploadPath)
    vertx.createHttpServer(new HttpServerOptions().setHost(host).setPort(port).setCompressionSupported(true))
      .requestHandler(new Handler[HttpServerRequest] {
      override def handle(event: HttpServerRequest): Unit = {
        router.accept(event)
      }
    }).listen()
    logger.info("Http Service is running at http://%s:%s".format(host, port))
    this
  }

  /**
   * 注册获取方法
   *
   * @param uri         地址
   * @param fun 处理方法
   */
  def get(uri: String, fun: => (MultiMap, Set[Cookie]) => Result[Any]) {
    logger.info("Add method [GET] url :" + uri)
    router.route(HttpMethod.GET, uri).handler(HttpServerProcessor.normalProcess(HttpMethod.GET, uri, fun))
  }

  /**
   * 注册删除方法
   *
   * @param uri         地址
   * @param fun 处理方法
   */
  def delete(uri: String, fun: => (MultiMap, Set[Cookie]) => Result[Any]) {
    logger.info("Add method [DELETE] url :" + uri)
    router.route(HttpMethod.DELETE, uri).handler(HttpServerProcessor.normalProcess(HttpMethod.DELETE, uri, fun))
  }

  /**
   * 注册添加方法
   *
   * @param uri         地址
   * @param fun 处理方法
   */
  def post[E](uri: String, requestClass: Class[E], fun: => (MultiMap, E, Set[Cookie]) => Result[Any]) {
    logger.info("Add method [POST] url :" + uri)
    router.route(HttpMethod.POST, uri).handler(HttpServerProcessor.submitProcess(HttpMethod.POST, uri, requestClass, fun))
  }


  /**
   * 注册更新方法
   *
   * @param uri         地址
   * @param fun 处理方法
   */
  def put[E](uri: String, requestClass: Class[E], fun: => (MultiMap, E, Set[Cookie]) => Result[Any]) {
    logger.info("Add method [PUT] url :" + uri)
    router.route(HttpMethod.PUT, uri).handler(HttpServerProcessor.submitProcess(HttpMethod.POST, uri, requestClass, fun))
  }

  /**
   * 注册上传方法
   *
   * @param uri         地址
   * @param fun 处理方法
   */
  def upload(uri: String, fun: => (MultiMap, Set[String], Set[Cookie]) => Result[Any]) {
    upload(uri, null, null, fun)
  }

  /**
   * 注册上传方法
   *
   * @param uri         地址
   * @param allowType   自定义允许的类型列表
   * @param uploadPath  自定义上传路径
   * @param fun 处理方法
   */
  def upload(uri: String, uploadPath: Option[String], allowType: Option[List[String]], fun: => (MultiMap, Set[String], Set[Cookie]) => Result[Any]) {
    logger.info("Add method [UPLOAD] url :" + uri)
    router.route(HttpMethod.POST, uri).handler(HttpServerProcessor.uploadProcess(uploadPath, allowType, fun))
  }

}
