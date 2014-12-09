package com.ecfront.rpc.socket.client

import com.ecfront.rpc.NettyClient
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}

/**
 * Socket客户端
 */
class SocketClient extends NettyClient {

  private val socketClientHandler = new SocketClientHandler

  protected override def addChannelHandler(pipeLine: ChannelPipeline): Unit = {
    pipeLine.addLast("decoder", new ObjectEncoder())
    pipeLine.addLast("encoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
    pipeLine.addLast(socketClientHandler)
  }

  /**
   * 发送消息
   * @param msg 消息内容
   * @return
   */
  def send(msg: Any) = {
    socketClientHandler.sendMessage = msg
    this
  }

  /**
   * 回复（来自服务器）的消息
   * @param function 自定义处理函数
   * @return
   */
  def reply(function: SocketClientFun[_]) = {
    socketClientHandler.replyFunction = function
    this
  }

}
