package com.ecfront.rpc.socket.client

import com.ecfront.rpc.RPC.Result

/**
 * 处理客户端接收到的消息，是Result的String字符串
 * @param bodyClazz 消息体类型
 * @tparam E 消息体类型
 */
abstract class SocketClientFun[E](bodyClazz: Class[E]) {

  private[socket] def innerExecute(message: Result[_]): Any = {
    if (null != message) {
      execute(message.code, message.body.asInstanceOf[E], message.message)
    } else {
      execute(null, null.asInstanceOf[E], null)
    }
  }

  /**
   * 自定义处理函数，由具体业务自行实现
   * @param code 结果状态码
   * @param body 消息主体
   * @param message 消息，多用于错误说明
   * @return
   */
  def execute(code: String, body: E, message: String): Any
}

