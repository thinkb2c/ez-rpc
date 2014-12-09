package com.ecfront.rpc

import com.ecfront.rpc.http.client.HttpClient
import com.ecfront.rpc.http.server.HttpServer
import com.ecfront.rpc.socket.client.SocketClient
import com.ecfront.rpc.socket.server.SocketServer
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
     * @return 服务实例
     */
    def http(port: Int, host: String = "0.0.0.0"): HttpServer = {
      new HttpServer().startup(port, host).asInstanceOf[HttpServer]
    }

    /**
     * 创建Socket服务
     * @param port 端口
     * @param host 主机
     * @return 服务实例
     */
    def socket(port: Int, host: String = "0.0.0.0"): SocketServer = {
      new SocketServer().startup(port, host).asInstanceOf[SocketServer]
    }
  }

  /**
   * 创建一个客户端连接
   */
  object Client {

    /**
     * 创建HTTP客户端实例
     * @return  客户端实例
     */
    def http: HttpClient = {
      HttpClient()
    }

    /**
     * 创建Socket客户端实例
     * @param port 端口
     * @param host 主机
     * @return 客户端实例
     */
    def socket(port: Int, host: String = "0.0.0.0"): SocketClient = {
      val client = new SocketClient()
      client.port = port
      client.host = host
      client.asInstanceOf[SocketClient]
    }
  }

  /**
   * 统一返回结果
   * @param code  结果状态码
   * @param body 业务结果（消息主体）
   * @param message 消息，多用于错误说明
   * @tparam E 业务结果的类型
   */
  case class Result[E](code: String, body: E, message: String)

  object Result extends LazyLogging {

    private[rpc] val CODE = "code"
    private[rpc] val BODY = "body"
    private[rpc] val MESSAGE = "message"

    def success[E](body: E) = new Result[E](Code.SUCCESS, body, null)

    def notFound(message: String) = {
      logger.warn("[Result]Not found:" + message)
      new Result[String](Code.NOT_FOUND, null, message)
    }

    def badRequest(message: String) = {
      logger.warn("[Result]Bad request:" + message)
      new Result[String](Code.BAD_REQUEST, null, message)
    }

    def unAuthorized(message: String) = {
      logger.warn("[Result]Unauthorized:" + message)
      new Result[String](Code.UNAUTHORIZED, null, message)
    }

    def serverError(message: String) = {
      logger.error("[Result]Server error:" + message)
      new Result[String](Code.INTERNAL_SERVER_ERROR, null, message)
    }

    /**
     * 结果状态码
     */
    object Code extends Enumeration {
      type HttpCode = Value
      val SUCCESS = Value("200").toString
      val BAD_REQUEST = Value("400").toString
      val UNAUTHORIZED = Value("401").toString
      val NOT_FOUND = Value("404").toString
      val INTERNAL_SERVER_ERROR = Value("500").toString
    }

  }

}
