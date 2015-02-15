package com.ecfront.rpc.http

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class PerformanceSpec extends FunSuite {

  test("HTTP性能测试") {

    val httpServer = RPC.Server.http(3000)

    httpServer.put[TestModel]("/index/:id/", classOf[TestModel], {
      (param, body, cookie) =>
        assert(param.get("id") == "test")
        Result.success(body)
    })

    val httpClient = RPC.Client.http
    val latch = new CountDownLatch(10000)
    val start = System.currentTimeMillis()
    for (i <- 0 to 10000) {
      new Thread(new Runnable {
        override def run(): Unit = {
          httpClient.putAsync[TestModel]("http://127.0.0.1:3000/index/test/", TestModel("测试"), classOf[TestModel], {
            result =>
              assert(result.code == "200")
              assert(result.body.name == "测试")
              latch.countDown()
          })
        }
      }).start()
    }
    latch.await()
    println("Total use：" + (System.currentTimeMillis() - start) / 1000 + "s")
  }
}
