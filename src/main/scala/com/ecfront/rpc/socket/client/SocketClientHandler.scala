package com.ecfront.rpc.socket.client

import com.ecfront.common.ScalaJsonHelper
import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

import scala.runtime.BoxedUnit

private[rpc] class SocketClientHandler extends ChannelInboundHandlerAdapter with LazyLogging {

  var sendMessage: Any = _
  var replyFunction: SocketClientFun[_] = _

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    ctx.writeAndFlush(sendMessage)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    if (replyFunction != null) {
      val result = replyFunction.innerExecute(msg.asInstanceOf[Result[_]])
      if (!result.isInstanceOf[BoxedUnit]) {
        ctx.write(ScalaJsonHelper.toJsonString(result))
      } else {
        ctx.close()
      }
    } else {
      ctx.close()
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



