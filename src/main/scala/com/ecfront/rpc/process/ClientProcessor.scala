package com.ecfront.rpc.process

import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * 连接处理器
 */
trait ClientProcessor extends LazyLogging {

  protected var port: Int = _
  protected var host: String = _

  private[rpc] def init(_port: Int, _host: String) {
    port = _port
    host = _host
    init()
  }

  /**
   * 初始化连接
   */
  protected def init()

  /**
   * 处理Result包装返回类型
   * @param method  资源操作方式
   * @param path 资源路径
   * @param requestBody  请求对象
   * @param responseClass 返回对象的类型
   * @param fun 业务方法
   * @return Result包装对象
   */
  protected[rpc] def process[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => Result[E] => Unit): Unit

  /**
   * 处理原生返回类型
   * @param method  资源操作方式
   * @param path 资源路径
   * @param requestBody  请求对象
   * @param responseClass 返回对象的类型
   * @param fun 业务方法
   * @return 原生对象
   */
  protected[rpc] def processRaw[E](method: String, path: String, requestBody: Any, responseClass: Class[E], fun: => E => Unit): Unit

}
