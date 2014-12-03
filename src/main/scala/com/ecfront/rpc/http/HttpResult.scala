package com.ecfront.rpc.http

import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * HTTP返回结果的统一数据结果
 * @param code  HTTP 标准码
 * @param result 业务结果
 * @param message 消息，多用于错误说明
 * @tparam E 业务结果的类型
 */
case class HttpResult[E](val code: String, val result: E, val message: String)

object HttpResult extends LazyLogging {

  def success[E](result: E) = new HttpResult[E](HttpCode.SUCCESS.toString, result, null)

  def notFound(message: String) = {
    logger.warn("[Result]Not found:" + message)
    new HttpResult[String](HttpCode.NOT_FOUND.toString, null, message)
  }

  def badRequest(message: String) = {
    logger.warn("[Result]Bad request:" + message)
    new HttpResult[String](HttpCode.BAD_REQUEST.toString, null, message)
  }

  def unAuthorized(message: String) = {
    logger.warn("[Result]Unauthorized:" + message)
    new HttpResult[String](HttpCode.UNAUTHORIZED.toString, null, message)
  }

  def serverError(message: String) = {
    logger.error("[Result]Server error:" + message)
    new HttpResult[String](HttpCode.INTERNAL_SERVER_ERROR.toString, null, message)
  }

}




