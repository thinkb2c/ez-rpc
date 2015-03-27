package com.ecfront.rpc

/**
 * 用户操作入口函数
 */
object RPC {

  /**
   * 创建一个服务器
   */
  def server = new Server

  /**
   * 创建一个连接客户端
   */
  def client = new Client


}
