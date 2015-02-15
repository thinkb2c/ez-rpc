package com.ecfront.rpc.akka.server

import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * AKKA服务器<br/>
 * 返回结果为统一的Result对象
 */
class AkkaServer extends LazyLogging {

  private[rpc] def startup(port: Int, host: String) = {
    logger.info("Akka Service starting")
    AkkaServerProcessor.init(port, host)
    logger.info("Akka Service is running at akka://%s:%s".format(host, port))
    this
  }

  /**
   * 注册处理方法
   * @param fun 处理方法
   */
  def process(fun: => AkkaRequest => Result[Any] = null) {
    AkkaServerProcessor.process(fun)
  }

}
