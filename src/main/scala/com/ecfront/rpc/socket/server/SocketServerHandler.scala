package com.ecfront.rpc.socket.server

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

import scala.runtime.BoxedUnit

private[rpc] class SocketServerHandler(id: String) extends ChannelInboundHandlerAdapter with LazyLogging {

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    val result = SocketServerFunctionContainer.getFunction(id).innerExecute(msg)
    if (!result.isInstanceOf[BoxedUnit]) {
      ctx.write(result)
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.error("", cause)
    ctx.close
  }


}



