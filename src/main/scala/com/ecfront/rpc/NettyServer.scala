package com.ecfront.rpc

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelInitializer, ChannelOption, ChannelPipeline}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

/**
 * 服务器基类，用于启动Netty服务器
 */
private[rpc] trait NettyServer extends LazyLogging {

  private val bossGroup = new NioEventLoopGroup(1)
  private val workerGroup = new NioEventLoopGroup()

  protected def addChannelHandler(pipeLine: ChannelPipeline)

  def startup(port: Int, host: String = "0.0.0.0") = {
    val b = new ServerBootstrap
    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .option[java.lang.Integer](ChannelOption.SO_BACKLOG, 100)
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        addChannelHandler(ch.pipeline())
      }
    })
    b.bind(host, port).sync.channel
    logger.info("Server startup at:host:" + host + ",port:" + port)
    this
  }

  def destroy() = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
    logger.info("Server destroy.")
  }
}
