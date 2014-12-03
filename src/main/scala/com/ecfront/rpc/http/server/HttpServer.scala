package com.ecfront.rpc.http.server

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.cors.{CorsConfig, CorsHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup

/**
 * 启动类，用于Netty服务<br/>
 * 支持标准的基于Json的Restful风格，返回结果为统一的HttpResult对象
 * @see com.ecfront.rpc.http.HttpResult
 */
object HttpServer extends LazyLogging {

  val bossGroup = new NioEventLoopGroup(1)
  val workerGroup = new NioEventLoopGroup()

  def startup(port: Int) {
    val b = new ServerBootstrap
    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        ch.pipeline()
          .addLast(new HttpRequestDecoder)
          .addLast(new HttpResponseEncoder)
          .addLast(new HttpObjectAggregator(65536))
          .addLast(new ChunkedWriteHandler())
          .addLast(new CorsHandler(CorsConfig.withAnyOrigin().build()))
          .addLast(new DefaultEventExecutorGroup(100), new HttpServerHandler)
      }
    })
    b.bind(port).sync.channel
    logger.info("Http service startup at:http://127.0.0.1:" + port + "/")
  }

  def destroy = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
