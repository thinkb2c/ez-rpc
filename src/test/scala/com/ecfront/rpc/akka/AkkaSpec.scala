package com.ecfront.rpc.akka

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.akka.server.AkkaRequest
import org.scalatest.FunSuite

class AkkaSpec extends FunSuite {

  test("AKKA服务及客户端测试") {

    val akkaServer = RPC.Server.akka(3000)

    val latch = new CountDownLatch(2)

    akkaServer.process({
      req =>
        Result.success(req.body)
    })

    val akkaClient = RPC.Client.akka(3000)

    akkaClient.process[String](AkkaRequest("get", "测试"), {
      result =>
        assert(result.code == "200")
        assert(result.body == "测试")
        latch.countDown()
    })

    akkaClient.process[TestModel](AkkaRequest("get", TestModel("测试")), {
      result =>
        assert(result.code == "200")
        assert(result.body.name == "测试")
        latch.countDown()
    })

    latch.await()

  }
}

case class TestModel(name: String)

