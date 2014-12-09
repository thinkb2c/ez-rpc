package com.ecfront.rpc.socket.server

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

import scala.runtime.BoxedUnit

private[rpc] class SocketServerHandler extends ChannelInboundHandlerAdapter with LazyLogging {

  var processFunction: SocketServerFun[_] = _

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    val result = processFunction.innerExecute(msg)
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



