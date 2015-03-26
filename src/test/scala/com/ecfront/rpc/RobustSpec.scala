package com.ecfront.rpc

import java.util.concurrent.CountDownLatch

import org.scalatest.FunSuite

class RobustSpec extends FunSuite {

  test("Robust测试") {
    // jsonFunTest(highPerformance = false)
    jsonFunTest(highPerformance = true)
  }

  def jsonFunTest(highPerformance: Boolean) {

    val latch = new CountDownLatch(100)
    val server = RPC.server.setPort(808).useHighPerformance().startup()
      .post[String]("/index/", classOf[String], {
      (param, body) =>
        latch.countDown()
        new Exception("error")
    })

    val client = RPC.client.setPort(808)
    if (highPerformance) client.useHighPerformance()
    client.startup()

    for (i <- 0 to 100) {
      new Thread(new Runnable {
        override def run(): Unit = {
          client.postSync[String]("/index/", "测试", classOf[String])
        }
      }).start()
    }

    latch.await()



    server.shutdown()
  }

}




