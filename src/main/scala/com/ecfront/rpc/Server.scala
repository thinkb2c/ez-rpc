package com.ecfront.rpc

import com.ecfront.rpc.akka.server.AkkaServerProcessor
import com.ecfront.rpc.autobuilding.AutoBuildingProcessor
import com.ecfront.rpc.http.server.HttpServerProcessor
import com.ecfront.rpc.process.ServerProcessor
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * RPC服务<br/>
 * 支持标准的基于Json的Restful风格，返回结果为统一的Result或自定义对象
 */
class Server extends LazyLogging {

  private var rootUploadPath = "/tmp/"
  private var port = 8080
  private var host = "0.0.0.0"
  private var processor: ServerProcessor = _
  private var highPerformance = false
  private val router = new Router


  def isHighPerformance: Boolean = {
    highPerformance
  }

  def getChannel: String = {
    if (highPerformance) "AKKA" else "HTTP"
  }

  /**
   * 设置服务通道，支持http及akka，http通道网络穿透性高，akka通道性能高，默认为false
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
   * 设置上传文件的根路径，只对http通道有效，默认为/tmp/
   * @param _rootUploadPath 上传文件的根路径
   */
  def setRootUploadPath(_rootUploadPath: String) = {
    rootUploadPath = _rootUploadPath
    this
  }

  /**
   * 启动服务
   */
  def startup(): Server = {
    logger.info("RPC Service starting")
    if (highPerformance) {
      processor = new AkkaServerProcessor
    } else {
      processor = new HttpServerProcessor
    }
    processor.init(port, host, router, rootUploadPath)
    logger.info("RPC Service is running at %s:%s:%s".format(if (highPerformance) "akka" else "http", host, port))
    this
  }

  /**
   * 关闭服务
   */
  def shutdown() {
    processor.destroy()
  }

  /**
   * 使用基于注解的自动构建，此方法必须在服务启动“startup”后才能调用
   * @param instance 目标对象
   * @param formatUrl 格式化URL
   * @param preExecute 前置执行方法
   * @param postExecute 后置执行方法
   */
  def autoBuilding(instance: AnyRef, formatUrl: => String => String = { String => String }, preExecute: => (Map[String, String], Any) => Boolean = { (parameter, body) => true }, postExecute: => Any => Any = { Any => Any }) = {
    AutoBuildingProcessor.process(this, instance, formatUrl, preExecute, postExecute)
    this
  }

  /**
   * 注册添加资源的方法
   * @param path 资源路径
   * @param requestClass 请求对象的类型
   * @param function 业务方法
   */
  def post[E](path: String, requestClass: Class[E], function: (Map[String, String], E) => Any) = {
    router.add("POST", path, requestClass, function, getChannel)
    this
  }

  /**
   * 注册更新资源的方法
   * @param path 资源路径
   * @param requestClass 请求对象的类型
   * @param function 业务方法
   */
  def put[E](path: String, requestClass: Class[E], function: => (Map[String, String], E) => Any) = {
    router.add("PUT", path, requestClass, function, getChannel)
    this
  }


  /**
   * 注册删除资源的方法
   * @param path 资源路径
   * @param function 业务方法
   */
  def delete(path: String, function: => (Map[String, String], Void) => Any) = {
    router.add("DELETE", path, classOf[Void], function, getChannel)
    this
  }


  /**
   * 注册获取资源的方法
   * @param path 资源路径
   * @param function 业务方法
   */
  def get(path: String, function: => (Map[String, String], Void) => Any) = {
    router.add("GET", path, classOf[Void], function, getChannel)
    this
  }

  /**
   * 反射调用，反射时避免类型type处理
   */
  object reflect {
    /**
     * 注册添加资源的方法
     * @param path 资源路径
     * @param requestClass 请求对象的类型
     * @param function 业务方法
     */
    def post(path: String, requestClass: Class[_], function: (Map[String, String], Any) => Any) = {
      router.add("POST", path, requestClass, function, getChannel)
      this
    }

    /**
     * 注册更新资源的方法
     * @param path 资源路径
     * @param requestClass 请求对象的类型
     * @param function 业务方法
     */
    def put(path: String, requestClass: Class[_], function: => (Map[String, String], Any) => Any) = {
      router.add("PUT", path, requestClass, function, getChannel)
      this
    }


    /**
     * 注册删除资源的方法
     * @param path 资源路径
     * @param function 业务方法
     */
    def delete(path: String, function: => (Map[String, String], Void) => Any) = {
      router.add("DELETE", path, classOf[Void], function, getChannel)
      this
    }


    /**
     * 注册获取资源的方法
     * @param path 资源路径
     * @param function 业务方法
     */
    def get(path: String, function: => (Map[String, String], Void) => Any) = {
      router.add("GET", path, classOf[Void], function, getChannel)
      this
    }
  }

  /* /**
    * 注册上传方法
    *
    * @param uri         地址
    * @param fun 处理方法
    */
   def upload(uri: String, fun: => (Map[String, String], Class[_], _) => Result[Any]) {
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
   def upload(uri: String, uploadPath: Option[String], allowType: Option[List[String]], fun: => (Map[String, String], Class[_], _) => Result[Any]) {
     logger.info("Add method [UPLOAD] url :" + uri)
     router.route(HttpMethod.POST, uri).handler(HttpServerProcessor.uploadProcess(uploadPath, allowType, fun))
   }*/

}
