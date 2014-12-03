
package com.ecfront.rpc.http

/**
 * 业务方法注册类
 */
object Register {

  //method与uri分隔符
  private val SPLIT: String = "@"

  //业务方法容器
  private val funContainer = new collection.mutable.HashMap[String, Fun[_]]

  //获取对应的方法
  private[http] def getFunction(method: String, uri: String): Fun[_] = {
    funContainer.get(method.toUpperCase() + SPLIT + uri).orNull
  }

  /**
   * 注册POST方法
   * @param uri uri
   * @param function 业务方法
   * @see com.ecfront.rpc.http.Fun
   */
  def post(uri: String, function: Fun[_]) {
    funContainer += ("POST" + SPLIT + uri -> function)
  }

  /**
   * 注册PUT方法
   * @param uri uri
   * @param function 业务方法
   * @see com.ecfront.rpc.http.Fun
   */
  def put(uri: String, function: Fun[_]) {
    funContainer += ("PUT" + SPLIT + uri -> function)
  }

  /**
   * 注册DELETE方法
   * @param uri uri
   * @param function 业务方法
   * @see com.ecfront.rpc.http.SimpleFun
   */
  def delete(uri: String, function: SimpleFun) {
    funContainer += ("DELETE" + SPLIT + uri -> function)
  }

  /**
   * 注册GET方法
   * @param uri uri
   * @param function 业务方法
   * @see com.ecfront.rpc.http.SimpleFun
   */
  def get(uri: String, function:SimpleFun) {
    funContainer += ("GET" + SPLIT + uri -> function)
  }

}











