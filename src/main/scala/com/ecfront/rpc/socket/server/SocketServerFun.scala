package com.ecfront.rpc.socket.server

import com.ecfront.rpc.RPC.Result

/**
 * 处理服务器接收到的消息
 * @param bodyClazz 消息体类型
 * @tparam E 消息体类型
 */
abstract class SocketServerFun[E](bodyClazz: Class[E]) {

  private[socket] def innerExecute(body: Any): Result[_] = {
    if (null != body) {
      execute(body.asInstanceOf[E])
    } else {
      execute(null.asInstanceOf[E])
    }
  }

  /**
   * 自定义处理函数，由具体业务自行实现
   * @param body 消息主体
   * @return
   */
  def execute(body: E): Result[_]
}

