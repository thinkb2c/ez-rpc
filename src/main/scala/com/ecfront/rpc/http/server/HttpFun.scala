package com.ecfront.rpc.http.server

import com.ecfront.common.ScalaJsonHelper
import com.ecfront.rpc.RPC.Result
import io.netty.handler.codec.http.Cookie

/**
 * 处理服务器接收到的消息
 * @param bodyClazz  消息体类型
 * @tparam E 消息体类型
 */
abstract class HttpFun[E](bodyClazz: Class[E]) {

  /**
   * 内部执行方法，由HttpServerHandler调用
   * @param parameters url参数列表
   * @param body request content内容
   * @param cookies cookies列表
   * @return 封装后返回结果
   */
  private[http] def innerExecute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): Result[_] = {
    if (null != body) {
      execute(parameters, ScalaJsonHelper.toObject[E](body, bodyClazz), cookies)
    } else {
      execute(parameters, null.asInstanceOf[E], cookies)
    }
  }

  /**
   * 自定义处理函数，由具体业务自行实现
   * @param parameters url参数列表
   * @param body request content内容
   * @param cookies cookies列表
   * @return
   */
  def execute(parameters: Map[String, String], body: E, cookies: Set[Cookie]): Result[_]
}

/**
 * 为GET、DELETE方法使用的简单业务类
 */
abstract class SimpleHttpFun extends HttpFun[String](classOf[String])