package com.ecfront.rpc.http.client

import java.util.Calendar

import com.ecfront.common.ScalaJsonHelper
import com.ecfront.rpc.http.HttpResult
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.http.client.methods._
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{BasicCookieStore, HttpClients}
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.util.EntityUtils

/**
 * HTTP 客户端<br/>
 * 支持标准的基于Json的Restful风格，返回结果为统一的HttpResult对象
 * @see com.ecfront.rpc.http.HttpResult
 *      //TODO 改由Netty实现
 */
class HttpClient extends LazyLogging {

  private val cookieStore = new BasicCookieStore()
  private val httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build()

  def get[E](url: String, bodyClass: Class[E]): HttpResult[E] = {
    baseRequest[E](new HttpGet(url), bodyClass)
  }

  def delete[E](url: String, bodyClass: Class[E]): HttpResult[E] = {
    baseRequest[E](new HttpDelete(url), bodyClass)
  }

  def post[E](url: String, data: Any, bodyClass: Class[E]): HttpResult[E] = {
    val httpPost = new HttpPost(url)
    attachData(data, httpPost)
    baseRequest[E](httpPost, bodyClass)
  }

  def put[E](url: String, data: Any, bodyClass: Class[E]): HttpResult[E] = {
    val httpPut = new HttpPut(url)
    attachData(data, httpPut)
    baseRequest[E](httpPut, bodyClass)
  }

  def addCookie(key: String, value: String = null, domain: String = null, path: String = null, expiryMinute: Int = 43200) = {
    val cookie = new BasicClientCookie(key, value)
    if (null != domain) {
      cookie.setDomain(domain)
    }
    if (null != path) {
      cookie.setPath(path)
    }
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, expiryMinute)
    cookie.setExpiryDate(calendar.getTime)
    cookieStore.addCookie(cookie)
    this
  }

  def removeCookie(key: String) = {
    addCookie(key, expiryMinute = -1)
    this
  }

  def clearCookies() = {
    cookieStore.clear()
    this
  }


  private def attachData(data: Any, request: HttpEntityEnclosingRequestBase): Unit = {
    request.setEntity(new StringEntity(ScalaJsonHelper.toJsonString(data), ContentType.APPLICATION_JSON))
  }

  private def baseRequest[E](request: HttpRequestBase, bodyClass: Class[E]): HttpResult[E] = {
    val context = HttpClientContext.create()
    context.setCookieStore(cookieStore)
    val response = httpClient.execute(request, context)
    returnJson[E](response, bodyClass)
  }

  private def returnJson[E](response: CloseableHttpResponse, bodyClass: Class[E]): HttpResult[E] = {
    val entity = response.getEntity
    val ret = if (null != entity) EntityUtils.toString(response.getEntity, "UTF-8") else ""
    response.close()
    val json = ScalaJsonHelper.toJson(ret)
    val code = json.path("code").asText()
    val body = ScalaJsonHelper.toObject[E](ScalaJsonHelper.toJsonString(json.path("body")), bodyClass)
    val message = json.path("body").asText()
    HttpResult(code, body, message)
  }
}

object HttpClient {

  def apply(): HttpClient = new HttpClient

}
