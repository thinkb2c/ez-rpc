package com.ecfront.rpc.http

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class HttpSpec extends FunSuite {

  test("HTTP服务及客户端测试") {

    val httpServer = RPC.Server.http(3000)


    val latch = new CountDownLatch(1)
    httpServer.get("/index/", {
      (param, cookie) =>
        Result.success("完成")
    })

    httpServer.post[String]("/index/", classOf[String], {
      (param, body, cookie) =>
        Result.success(body)
    })

    httpServer.put[TestModel]("/index/", classOf[TestModel], {
      (param, body, cookie) =>
        Result.success(body)
    })

    httpServer.upload("/upload/", {
      (param, files, cookie) =>
        Result.success(files)
    })

    latch.await()

  }
}

case class TestModel(name: String)

