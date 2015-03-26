package com.ecfront.rpc

import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.akka.client.AkkaClientProcessor
import com.ecfront.rpc.http.client.HttpClientProcessor
import com.ecfront.rpc.process.ClientProcessor
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * 连接客户端<br/>
 * 支持标准的基于Json的Restful风格，返回结果为统一的Result或自定义对象
 */
class Client extends LazyLogging {

  private var port = 8080
  private var host = "0.0.0.0"
  private var processor: ClientProcessor = _
  private var highPerformance = false

  /**
   * 设置服务通道，支持http及akka，http通道网络穿透性高，akka通道性能高，默认为true
   */
  def useHighPerformance() = {
    highPerformance = true
    this
  }

  /**
   * 设置服务端口，默认为8080
   * @param _port 服务端口
   */
  def setPort(_port: Int) = {
    port = _port
    this
  }

  /**
   * 设置主机名，默认为0.0.0.0
   * @param _host 主机名
   */
  def setHost(_host: String) = {
    host = _host
    this
  }

  /**
   * 开启连接
   */
  def startup(): Client = {
    if (highPerformance) {
      processor = new AkkaClientProcessor
    } else {
      processor = new HttpClientProcessor
    }
    processor.init(port, host)
    this
  }

  /**
   * 获取资源
   * @param path 资源路径
   * @param responseClass 返回对象的类型
   * @param fun 业务方法
   * @return Result包装对象
   */
  def get[E](path: String, responseClass: Class[E] = null, fun: => Result[E] => Unit = null) = {
    processor.process[E]("GET", path, null, responseClass, fun)
    this
  }

  /**
   * 获取资源（同步）
   * @param path 资源路径
   * @param responseClass 返回对象的类型
   * @return Result包装对象
   */
  def getSync[E](path: String, responseClass: Class[E] = null): Option[Result[E]] = {
    Await.result(processor.process[E]("GET", path, null, responseClass), Duration.Inf)
  }

  /**
   * 删除资源
   * @param path 资源路径
   * @param responseClass 返回对象的类型
   * @param fun 业务方法
   * @return Result包装对象
   */
  def delete[E](path: String, responseClass: Class[E] = null, fun: => Result[E] => Unit = null) = {
    processor.process[E]("DELETE", path, null, responseClass, fun)
    this
  }

  /**
   * 删除资源（同步）
   * @param path 资源路径
   * @param responseClass 返回对象的类型
   * @return Result包装对象
   */
  def deleteSync[E](path: String, responseClass: Class[E] = null): Option[Result[E]] = {
    Await.result(processor.process[E]("DELETE", path, null, responseClass), Duration.Inf)
  }

  /**
   * 添加资源
   * @param path 资源路径
   * @param data 资源对象
   * @param responseClass 返回对象的类型
   * @param fun 业务方法
   * @return Result包装对象
   */
  def post[E](path: String, data: Any, responseClass: Class[E] = null, fun: => Result[E] => Unit = null) = {
    processor.process[E]("POST", path, data, responseClass, fun)
    this
  }

  /**
   * 添加资源（同步）
   * @param path 资源路径
   * @param data 资源对象
   * @param responseClass 返回对象的类型
   * @return Result包装对象
   */
  def postSync[E](path: String, data: Any, responseClass: Class[E] = null): Option[Result[E]] = {
    Await.result(processor.process[E]("POST", path, data, responseClass), Duration.Inf)
  }

  /**
   * 更新资源
   * @param path 资源路径
   * @param data 资源对象
   * @param responseClass 返回对象的类型
   * @param fun 业务方法
   * @return Result包装对象
   */
  def put[E](path: String, data: Any, responseClass: Class[E] = null, fun: => Result[E] => Unit = null) = {
    processor.process[E]("PUT", path, data, responseClass, fun)
    this
  }

  /**
   * 更新资源（同步）
   * @param path 资源路径
   * @param data 资源对象
   * @param responseClass 返回对象的类型
   * @return Result包装对象
   */
  def putSync[E](path: String, data: Any, responseClass: Class[E] = null): Option[Result[E]] = {
    Await.result(processor.process[E]("PUT", path, data, responseClass), Duration.Inf)
  }

  /**
   * 返回原生对象的资源操作
   */
  def raw = new Raw

  class Raw {

    /**
     * 获取资源
     * @param path 资源路径
     * @param responseClass 返回对象的类型
     * @param fun 业务方法
     * @return 原生对象
     */
    def get[E](path: String, responseClass: Class[E] = null, fun: => E => Unit = null) = {
      processor.processRaw[E]("GET", path, "", responseClass, fun)
      this
    }

    /**
     * 获取资源（同步）
     * @param path 资源路径
     * @param responseClass 返回对象的类型
     * @return 原生对象
     */
    def getSync[E](path: String, responseClass: Class[E] = null): Option[E] = {
      Await.result(processor.processRaw[E]("GET", path, "", responseClass), Duration.Inf)
    }

    /**
     * 删除资源
     * @param path 资源路径
     * @param responseClass 返回对象的类型
     * @param fun 业务方法
     * @return 原生对象
     */
    def delete[E](path: String, responseClass: Class[E] = null, fun: => E => Unit = null) = {
      processor.processRaw[E]("DELETE", path, "", responseClass, fun)
      this
    }

    /**
     * 删除资源（同步）
     * @param path 资源路径
     * @param responseClass 返回对象的类型
     * @return 原生对象
     */
    def deleteSync[E](path: String, responseClass: Class[E] = null): Option[E] = {
      Await.result(processor.processRaw[E]("DELETE", path, "", responseClass), Duration.Inf)
    }

    /**
     * 添加资源
     * @param path 资源路径
     * @param data 资源对象
     * @param responseClass 返回对象的类型
     * @param fun 业务方法
     * @return 原生对象
     */
    def post[E](path: String, data: Any, responseClass: Class[E] = null, fun: => E => Unit = null) = {
      processor.processRaw[E]("POST", path, data, responseClass, fun)
      this
    }

    /**
     * 添加资源（同步）
     * @param path 资源路径
     * @param data 资源对象
     * @param responseClass 返回对象的类型
     * @return 原生对象
     */
    def postSync[E](path: String, data: Any, responseClass: Class[E] = null): Option[E] = {
      Await.result(processor.processRaw[E]("POST", path, data, responseClass), Duration.Inf)
    }

    /**
     * 更新资源
     * @param path 资源路径
     * @param data 资源对象
     * @param responseClass 返回对象的类型
     * @param fun 业务方法
     * @return 原生对象
     */
    def put[E](path: String, data: Any, responseClass: Class[E] = null, fun: => E => Unit = null) = {
      processor.processRaw[E]("PUT", path, data, responseClass, fun)
      this
    }

    /**
     * 更新资源（同步）
     * @param path 资源路径
     * @param data 资源对象
     * @param responseClass 返回对象的类型
     * @return 原生对象
     */
    def putSync[E](path: String, data: Any, responseClass: Class[E] = null): Option[E] = {
      Await.result(processor.processRaw[E]("PUT", path, data, responseClass), Duration.Inf)
    }
  }

}
