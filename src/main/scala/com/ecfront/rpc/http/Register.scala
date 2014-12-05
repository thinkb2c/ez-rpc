
package com.ecfront.rpc.http

import com.ecfront.rpc.http.server.FunctionContainer

/**
 * 业务方法注册类
 */
object Register {

  /**
   * 注册POST方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.Fun
   */
  def post(path: String, function: Fun[_]) {
    FunctionContainer.add("POST", path, function)
  }

  /**
   * 注册PUT方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.Fun
   */
  def put(path: String, function: Fun[_]) {
    FunctionContainer.add("PUT", path, function)
  }

  /**
   * 注册DELETE方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.SimpleFun
   */
  def delete(path: String, function: SimpleFun) {
    FunctionContainer.add("DELETE", path, function)
  }

  /**
   * 注册GET方法
   * @param path path
   * @param function 业务方法
   * @see com.ecfront.rpc.http.SimpleFun
   */
  def get(path: String, function: SimpleFun) {
    FunctionContainer.add("GET", path, function)
  }

}











