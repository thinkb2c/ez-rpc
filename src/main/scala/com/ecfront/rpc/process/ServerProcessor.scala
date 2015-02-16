package com.ecfront.rpc.process

import java.io.File

import com.ecfront.rpc.Router
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * 服务处理器
 */
trait ServerProcessor extends LazyLogging {

  protected var port: Int = _
  protected var host: String = _
  protected var router: Router = _
  protected var rootUploadPath: String = _

  private[rpc] def init(_port: Int, _host: String, _router: Router, _rootUploadPath: String) {
    port = _port
    host = _host
    router = _router
    rootUploadPath = _rootUploadPath
    if (!rootUploadPath.endsWith(File.separator)) {
      rootUploadPath += File.separator
    }
    init()
  }

  /**
   * 初始化服务
   */
  protected def init()

  /**
   * 销毁服务
   */
  private[rpc] def destroy()

}
