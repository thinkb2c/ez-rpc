package com.ecfront.rpc

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class MultiSpec extends FunSuite {

  test("多服务测试") {
    RPC.server.setChannel(false).setPort(8001).startup().get("/test/", {
      (param, _) =>
        Result.success("OK1")
    })
    RPC.server.setChannel(false).setPort(8002).startup().get("/test/", {
      (param, _) =>
        Result.success("OK2")
    })
    RPC.server.setChannel(true).setPort(8003).startup().get("/test/", {
      (param, _) =>
        Result.success("OK3")
    })
    RPC.server.setChannel(true).setPort(8004).startup().get("/test/", {
      (param, _) =>
        Result.success("OK4")
    })
    RPC.server.setChannel(true).setPort(8005).startup().get("/test/", {
      (param, _) =>
        Result.success("OK5")
    })
    val latch = new CountDownLatch(6)

    RPC.client.setChannel(false).setPort(8001).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK1")
        latch.countDown()
    })
    RPC.client.setChannel(false).setPort(8002).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK2")
        latch.countDown()
    })
    RPC.client.setChannel(true).setPort(8003).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK3")
        latch.countDown()
    })
    RPC.client.setChannel(true).setPort(8004).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK4")
        latch.countDown()
    })
    RPC.client.setChannel(true).setPort(8005).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK5")
        latch.countDown()
    })
    RPC.client.setChannel(true).setPort(8005).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK5")
        latch.countDown()
    })
    latch.await()
  }

}


