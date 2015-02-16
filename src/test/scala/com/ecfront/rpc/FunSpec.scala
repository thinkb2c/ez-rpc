package com.ecfront.rpc

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class FunSpec extends FunSuite {

  test("功能测试") {
    funTest(highPerformance = false)
    funTest(highPerformance = true)
  }

  def funTest(highPerformance: Boolean) {
    val latch = new CountDownLatch(6)

    val server = RPC.server.setChannel(highPerformance).startup()
      .get("/number/", {
      (param, _) =>
        Result.success(1L)
    }).get("/boolean/", {
      (param, _) =>
        Result.success(true)
    }).get("/index/", {
      (param, _) =>
        Result.success("完成")
    }).post[String]("/index/", classOf[String], {
      (param, body) =>
        Result.success(body)
    }).put[TestModel]("/index/:id/", classOf[TestModel], {
      (param, body) =>
        assert(body.name == "测试")
        assert(param.get("id").get == "test")
        Result.success(body)
    }).put[TestModel]("/custom/:id/", classOf[TestModel], {
      (param, body) =>
        assert(body.name == "测试")
        assert(param.get("id").get == "test")
        //Result custom type
        body
    })

    /* httpServer.upload("/upload/", {
       (param, files, cookie) =>
         Result.success(files)
     })*/

    val client = RPC.client.setChannel(highPerformance).startup()
      .get[Long]("/number/", classOf[Long], {
      result =>
        assert(result.code == "200")
        assert(result.body == 1L)
        latch.countDown()
    }).get[Boolean]("/boolean/", classOf[Boolean], {
      result =>
        assert(result.code == "200")
        assert(result.body)
        latch.countDown()
    }).get[String]("/index/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "完成")
        latch.countDown()
    }).post[String]("/index/", "测试", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "测试")
        latch.countDown()
    }).put[TestModel]("/index/test/", TestModel("测试"), classOf[TestModel], {
      result =>
        assert(result.code == "200")
        assert(result.body.name == "测试")
        latch.countDown()
    }).put[TestModel]("/index/test/", TestModel("测试"))

    client.raw.put[TestModel]("/custom/test/", TestModel("测试"), classOf[TestModel], {
      result =>
        assert(result.name == "测试")
        latch.countDown()
    })
    latch.await()

    server.shutdown()
  }
}

case class TestModel(name: String)

