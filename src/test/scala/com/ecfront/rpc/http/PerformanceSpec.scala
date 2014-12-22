package com.ecfront.rpc.http

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC
import com.ecfront.rpc.http.server.SimpleHttpFun
import io.netty.handler.codec.http.Cookie
import org.scalatest.FunSuite

class PerformanceSpec extends FunSuite {

  test("HTTP性能测试") {

    val httpServer = RPC.Server.http(3000)

    httpServer.get("/user/", new SimpleHttpFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): RPC.Result[_] = {
        RPC.Result.success[String](parameters.get("arg").get)
      }
    })

    val client = RPC.Client.http

    val countDown = new CountDownLatch(1000)
    for (i <- 0 to 1000) {
      new Thread(new Runnable {
        override def run(): Unit = {
          val result = client.get("http://127.0.0.1:3000/user/?arg=测试", classOf[String])
          assert(result.body == "测试")
          countDown.countDown()
        }
      }).start()
    }
    countDown.await()
    httpServer.destroy
  }
}
