package com.ecfront.rpc.http

import com.ecfront.utils.ScalaJsonHelper
import io.netty.handler.codec.http.Cookie

/**
 * 业务基类
 * @param bodyClazz body的class
 * @tparam E body的类型
 */
abstract class Fun[E](bodyClazz: Class[E]) {

  /**
   * 内部执行方法，由HttpServerHandler调用
   * @param parameters url参数列表
   * @param body request content内容
   * @param cookies cookies列表
   * @return 封装后返回结果
   */
  private[http] def innerExecute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): HttpResult[_] = {
    if (null != body) {
      execute(parameters, ScalaJsonHelper.toObject[E](body, bodyClazz), cookies)
    } else {
      execute(parameters, null.asInstanceOf[E], cookies)
    }
  }

  /**
   * 执行方法，由具体业务自行实现
   * @param parameters url参数列表
   * @param body request content内容
   * @param cookies cookies列表
   * @return 封装后返回结果
   */
  def execute(parameters: Map[String, String], body: E, cookies: Set[Cookie]): HttpResult[_]
}

/**
 * 为GET、DELETE方法使用的简单业务类
 */
abstract class SimpleFun extends Fun[String](classOf[String])