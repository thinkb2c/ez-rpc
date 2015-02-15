package com.ecfront.rpc.akka

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.akka.server.AkkaRequest
import org.scalatest.FunSuite

class PerformanceSpec extends FunSuite {

  test("Akka性能测试") {

    val akkaServer = RPC.Server.akka(3000)

    akkaServer.process({
      req =>
        Result.success(req.body)
    })

    val akkaClient = RPC.Client.akka(3000)

    val latch = new CountDownLatch(10000)
    val start = System.currentTimeMillis()
    for (i <- 0 to 10000) {
      new Thread(new Runnable {
        override def run(): Unit = {
          akkaClient.process[TestModel](AkkaRequest("get", TestModel("测试")), {
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
