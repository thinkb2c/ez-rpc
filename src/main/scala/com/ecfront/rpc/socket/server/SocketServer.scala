package com.ecfront.rpc.socket.server

import com.ecfront.rpc.NettyServer
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}

/**
 * Socket服务器
 */
class SocketServer extends NettyServer {

  private val socketServerHandler = new SocketServerHandler

  protected override def addChannelHandler(pipeLine: ChannelPipeline): Unit = {
    pipeLine.addLast("decoder", new ObjectEncoder())
    pipeLine.addLast("encoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
    pipeLine.addLast(socketServerHandler)
  }

  def process(function: SocketServerFun[_]) = {
    socketServerHandler.processFunction = function
    this
  }
}
