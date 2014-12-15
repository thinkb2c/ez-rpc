package com.ecfront.rpc.http.server

import com.ecfront.common.ScalaJsonHelper
import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.netty.buffer.Unpooled._
import io.netty.channel.{Channel, ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

import scala.collection.JavaConversions._

private[rpc] class HttpServerHandler extends SimpleChannelInboundHandler[HttpObject] with LazyLogging {

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpObject): Unit = {
    msg match {
      case request: HttpRequest =>
        val url = new QueryStringDecoder(request.getUri)
        if (url.uri() != "/favicon.ico") {
          //根据method及uri查询是否有对应的业务方法
          val (function, parameters) = HttpFunctionContainer.getFunction(request.getMethod.toString, url.path())
          if (function != null) {
            val cookies = if (request.headers().get(COOKIE) != null) CookieDecoder.decode(request.headers().get(COOKIE)).toSet else Set[Cookie]()
            url.parameters().foreach {
              item =>
                parameters += (item._1 -> item._2(0))
            }
            var content: String = null
            if (request.getMethod == HttpMethod.POST || request.getMethod == HttpMethod.PUT) {
              content = msg.asInstanceOf[HttpContent].content().toString(CharsetUtil.UTF_8)
            }
            try {
              HttpServerHandler.responseJson(ctx.channel, request, HttpServerHandler.packageJsonResult(function.innerExecute(parameters.toMap, content, cookies)))
            } catch {
              case _: Throwable =>
                HttpServerHandler.responseJson(ctx.channel, request, HttpServerHandler.packageJsonResult(RPC.Result.serverError("服务处理错误")))
            }
          } else {
            HttpServerHandler.responseJson(ctx.channel, request, HttpServerHandler.packageJsonResult(RPC.Result.badRequest("没有对应的业务实现")))
          }
        }
      case _ =>
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.error("", cause)
    ctx.close
  }
}

private[rpc] object HttpServerHandler extends LazyLogging {

  private def responseJson(channel: Channel, req: HttpRequest, json: String): Unit = {
    response(channel, req, json, "application/json; charset=UTF-8")
  }

  private[this] def response(channel: Channel, req: HttpRequest, result: String, contentType: String) {
    val content = copiedBuffer(result, CharsetUtil.UTF_8)
    val res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content)
    res.headers.set(CONTENT_TYPE, contentType).set(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    val close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(req.headers().get(CONNECTION)) || req.getProtocolVersion.equals(HttpVersion.HTTP_1_0) && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(req.headers().get(CONNECTION))
    if (!close) {
      res.headers().set(CONTENT_LENGTH, content.readableBytes())
    }
    val future = channel.writeAndFlush(res)
    if (close) {
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def packageJsonResult(result: Result[_]): String = {
    ScalaJsonHelper.toJsonString(result)
  }
}

