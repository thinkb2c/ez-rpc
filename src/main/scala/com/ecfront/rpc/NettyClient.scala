package com.ecfront.rpc

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{ChannelInitializer, ChannelOption, ChannelPipeline}

/**
 * 客户端基类，用于启动Netty客户端
 */
private[rpc] trait NettyClient extends LazyLogging {

  private val group = new NioEventLoopGroup()

  var host: String = "0.0.0.0"
  var port: Int = _

  protected def addChannelHandler(pipeLine: ChannelPipeline)

  def startup() = {
    val b = new Bootstrap
    b.group(group)
      .channel(classOf[NioSocketChannel])
      .option[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)
      .handler(new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        addChannelHandler(ch.pipeline())
      }
    })
    logger.info("Client startup at:host:" + host + ",port:" + port)
    b.connect(host, port).sync.channel.closeFuture().awaitUninterruptibly()
    this
  }

  def destroy() = {
    group.shutdownGracefully()
    logger.info("Client destroy.")
  }
}
