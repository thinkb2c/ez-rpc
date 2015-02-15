package com.ecfront.rpc

import com.ecfront.rpc.akka.client.AkkaClient
import com.ecfront.rpc.akka.server.AkkaServer
import com.ecfront.rpc.http.client.HttpClient
import com.ecfront.rpc.http.server.HttpServer
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * 用户操作入口函数
 */
object RPC {

  /**
   * 创建一个服务器
   */
  object Server {

    /**
     * 创建HTTP服务
     * @param port 端口
     * @param host 主机
     * @param baseUploadPath 上传根目录
     * @return 服务实例
     */
    def http(port: Int, host: String = "0.0.0.0", baseUploadPath: String = "/tmp/"): HttpServer = {
      new HttpServer().startup(port, host, baseUploadPath)
    }

    /**
     * 创建Akka服务
     * @param port 端口
     * @param host 主机
     * @return 服务实例
     */
    def akka(port: Int, host: String = "0.0.0.0"): AkkaServer = {
      new AkkaServer().startup(port, host)
    }

  }

  /**
   * 创建客户端连接
   */
  object Client {

    /**
     * 创建HTTP客户端实例
     * @return  客户端实例
     */
    def http: HttpClient = {
      new HttpClient()
    }

    /**
     * 创建Akka客户端实例
     * @param port 端口
     * @param host 主机
     * @return 客户端实例
     */
    def akka(port: Int, host: String = "0.0.0.0"): AkkaClient = {
      new AkkaClient().startup(port, host)
    }

  }

  /**
   * 统一返回结果
   * @param code  结果状态码
   * @param _body 业务结果（消息主体）, 最终返回的是 body
   * @param message 消息，多用于错误说明
   * @tparam E 业务结果的类型
   */
  case class Result[E](code: String, message: String, private val _body: Option[E]) {
    var body: E = _
  }

  object Result extends LazyLogging {

    private[rpc] val CODE = "code"
    private[rpc] val BODY = "body"
    private[rpc] val MESSAGE = "message"

    def success[E](body: E) = {
      val result = new Result[E](Code.SUCCESS, null, Some(body))
      result.body = body
      result
    }

    def notFound[E](message: String) = {
      logger.warn("[Result] [%s] Not found: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.NOT_FOUND, message, null)
    }

    def badRequest[E](message: String) = {
      logger.warn("[Result] [%s] Bad request: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.BAD_REQUEST, message, null)
    }

    def forbidden[E](message: String) = {
      logger.warn("[Result] [%s] Forbidden: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.FORBIDDEN, message, null)
    }

    def unAuthorized[E](message: String) = {
      logger.warn("[Result] [%s] Unauthorized: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.UNAUTHORIZED, message, null)
    }

    def serverError[E](message: String) = {
      logger.error("[Result] [%s] Server error: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.INTERNAL_SERVER_ERROR, message, null)
    }

    def notImplemented[E](message: String) = {
      logger.error("[Result] [%s] Not implemented: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.NOT_IMPLEMENTED, message, null)
    }

    def serverUnavailable[E](message: String) = {
      logger.error("[Result] [%s] Server unavailable: %s".format(Code.NOT_FOUND, message))
      new Result[E](Code.SERVICE_UNAVAILABLE, message, null)
    }

    def customFail[E](code: String, message: String) = {
      logger.error("[Result] [%s] Custom fail: %s".format(Code.NOT_FOUND, message))
      new Result[E](code, message, null)
    }

    /**
     * 结果状态码
     */
    object Code extends Enumeration {
      type HttpCode = Value
      val SUCCESS = Value("200").toString
      val BAD_REQUEST = Value("400").toString
      val UNAUTHORIZED = Value("401").toString
      val FORBIDDEN = Value("403").toString
      val NOT_FOUND = Value("404").toString
      val INTERNAL_SERVER_ERROR = Value("500").toString
      val NOT_IMPLEMENTED = Value("501").toString
      val SERVICE_UNAVAILABLE = Value("503").toString
    }

  }

}
