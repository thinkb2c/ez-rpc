package com.ecfront.rpc

import java.util.concurrent.CountDownLatch

import com.ecfront.common.Resp
import org.scalatest.FunSuite

class MultiSpec extends FunSuite {

  test("多服务测试") {
    RPC.server.setPort(8001).startup().get("/test/", {
      (param, _, _) =>
        Resp.success("OK1")
    })
    RPC.server.setPort(8002).startup().get("/test/", {
      (param, _, _) =>
        Resp.success("OK2")
    })
    RPC.server.useHighPerformance().setPort(8003).startup().get("/test/", {
      (param, _, _) =>
        Resp.success("OK3")
    })
    RPC.server.useHighPerformance().setPort(8004).startup().get("/test/", {
      (param, _, _) =>
        Resp.success("OK4")
    })
    RPC.server.useHighPerformance().setPort(8005).startup().get("/test/", {
      (param, _, _) =>
        Resp.success("OK5")
    })
    val latch = new CountDownLatch(6)

    RPC.client.setPort(8001).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK1")
        latch.countDown()
    })
    RPC.client.setPort(8002).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK2")
        latch.countDown()
    })
    RPC.client.useHighPerformance().setPort(8003).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK3")
        latch.countDown()
    })
    RPC.client.useHighPerformance().setPort(8004).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK4")
        latch.countDown()
    })
    RPC.client.useHighPerformance().setPort(8005).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK5")
        latch.countDown()
    })
    RPC.client.useHighPerformance().setPort(8005).startup().get[String]("/test/", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "OK5")
        latch.countDown()
    })
    latch.await()
  }

}


