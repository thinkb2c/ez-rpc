package com.ecfront.rpc

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class PerformanceSpec extends FunSuite {


  test("性能测试") {
    perfTest(highPerformance = false)
    perfTest(highPerformance = true)
  }

  def perfTest(highPerformance: Boolean) {
    val server = RPC.server.setChannel(highPerformance).startup().put[TestModel]("/index/:id/", classOf[TestModel], {
      (param, body) =>
        Result.success(body)
    })

    val client = RPC.client.setChannel(highPerformance).startup()

    val latch = new CountDownLatch(5000)
    val start = System.currentTimeMillis()
    for (i <- 0 to 5000) {
      new Thread(new Runnable {
        override def run(): Unit = {
          client.put[TestModel]("/index/test/", TestModel("测试"), classOf[TestModel], {
            result =>
              assert(result.code == "200")
              assert(result.body.name == "测试")
              latch.countDown()
          })
        }
      }).start()
    }
    latch.await()
    println(">>>>>>> Total use：" + (System.currentTimeMillis() - start) / 1000 + "s")
    server.shutdown()
  }
}
