
package com.ecfront.rpc.socket.server

/**
 * 业务方法容器类
 */
private[rpc] object SocketServerFunctionContainer {

  //业务方法容器
  private val funContainer = collection.mutable.Map[String, SocketServerFun[_]]()

  /**
   * 获取对应的方法
   * @param id
   * @return
   */
  private[socket] def getFunction(id: String): SocketServerFun[_] = {
    funContainer.get(id).get
  }

  private[socket] def add(id: String, function: SocketServerFun[_]) {
    funContainer += (id -> function)
  }

}











