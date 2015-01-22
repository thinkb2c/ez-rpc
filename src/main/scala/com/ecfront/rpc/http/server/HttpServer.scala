package com.ecfront.rpc.http.server

import com.ecfront.rpc.NettyServer
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.cors.{CorsConfig, CorsHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup

/**
 * HTTP服务器，支持标准的基于Json的Restful风格，返回结果为统一的Result对象
 */
class HttpServer extends NettyServer {

  protected override def addChannelHandler(pipeLine: ChannelPipeline): Unit = {
    pipeLine
      .addLast(new HttpRequestDecoder)
      .addLast(new HttpResponseEncoder)
      .addLast(new HttpObjectAggregator(1048576 * 5)) //5MB
      .addLast(new ChunkedWriteHandler())
      .addLast(new CorsHandler(CorsConfig.withAnyOrigin().build()))
      .addLast(new DefaultEventExecutorGroup(100), new HttpServerHandler)
  }

  /**
   * 注册POST方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.Fun
   */
  def post(path: String, function: HttpFun[_]) = {
    HttpFunctionContainer.add("POST", path, function)
    this
  }

  /**
   * 注册PUT方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.Fun
   */
  def put(path: String, function: HttpFun[_]) = {
    HttpFunctionContainer.add("PUT", path, function)
    this
  }

  /**
   * 注册DELETE方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.SimpleFun
   */
  def delete(path: String, function: SimpleHttpFun) = {
    HttpFunctionContainer.add("DELETE", path, function)
    this
  }

  /**
   * 注册GET方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.SimpleFun
   */
  def get(path: String, function: SimpleHttpFun) = {
    HttpFunctionContainer.add("GET", path, function)
    this
  }

}
