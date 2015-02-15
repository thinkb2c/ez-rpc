package com.ecfront.rpc.http

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class HttpSpec extends FunSuite {

  test("HTTP服务及客户端测试") {

    val httpServer = RPC.Server.http(3000)

    val latch = new CountDownLatch(5)

    httpServer.get("/number/", {
      (param, cookie) =>
        Result.success(1)
    })

    httpServer.get("/boolean/", {
      (param, cookie) =>
        Result.success(true)
    })

    httpServer.get("/index/", {
      (param, cookie) =>
        Result.success("完成")
    })

    httpServer.post[String]("/index/", classOf[String], {
      (param, body, cookie) =>
        Result.success(body)
    })

    httpServer.put[TestModel]("/index/:id/", classOf[TestModel], {
      (param, body, cookie) =>
        assert(param.get("id") == "test")
        Result.success(body)
    })

    httpServer.upload("/upload/", {
      (param, files, cookie) =>
        Result.success(files)
    })

    val httpClient = RPC.Client.http

    httpClient.getAsync[Long]("http://127.0.0.1:3000/number/", classOf[Long], {
      result =>
        assert(result.code == "200")
        assert(result.body == 1)
        latch.countDown()
    })

    httpClient.getAsync[Boolean]("http://127.0.0.1:3000/boolean/", classOf[Boolean], {
      result =>
        assert(result.code == "200")
        assert(result.body == true)
        latch.countDown()
    })

    httpClient.getAsync[String]("http://127.0.0.1:3000/index/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "完成")
        latch.countDown()
    })

    httpClient.postAsync[String]("http://127.0.0.1:3000/index/", "测试", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "测试")
        latch.countDown()
    })

    httpClient.putAsync[TestModel]("http://127.0.0.1:3000/index/test/", TestModel("测试"), classOf[TestModel], {
      result =>
        assert(result.code == "200")
        assert(result.body.name == "测试")
        latch.countDown()
    })

    httpClient.putAsync[TestModel]("http://127.0.0.1:3000/index/test/", TestModel("测试"))

    latch.await()

  }
}

case class TestModel(name: String)

