package com.ecfront.rpc.http

/**
 * HTTP 标准码
 */
object HttpCode extends Enumeration {
  type HttpCode = Value
  val SUCCESS=Value("200")
  val BAD_REQUEST=Value("400")
  val UNAUTHORIZED=Value("401")
  val NOT_FOUND=Value("404")
  val INTERNAL_SERVER_ERROR=Value("500")
}
