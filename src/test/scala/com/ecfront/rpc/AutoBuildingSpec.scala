package com.ecfront.rpc

import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.autobuilding._
import org.scalatest.FunSuite

class AutoBuildingSpec extends FunSuite {

  test("自动构建测试") {
    autoBuildingTest(highPerformance = false)
    autoBuildingTest(highPerformance = true)
  }

  def autoBuildingTest(highPerformance: Boolean) {
    val server = RPC.server.setPort(808)
    if (highPerformance) server.useHighPerformance()
    server.startup().autoBuilding(AService).autoBuilding(BService())

    val client = RPC.client.setPort(808)
    if (highPerformance) client.useHighPerformance()
    client.startup()
    assert(client.getSync[TestModel]("/test/1/", classOf[TestModel]).get.body.name == "测试")
    assert(client.postSync[String]("/test/", TestModel("测试"), classOf[String]).get.body == "OK")
    assert(client.putSync[String]("/test/1/", TestModel("测试"), classOf[String]).get.body == "OK")
    assert(client.deleteSync[String]("/test/1/", classOf[String]).get.body == "OK")

    server.shutdown()
  }

}


object AService {

  @post(uri = "/test/", http = true, akka = true)
  def postTest(parameter: Map[String, String], req: TestModel): Result[String] = {
    assert(req.name == "测试")
    Result.success("OK")
  }

  @put(uri = "/test/:id/", http = true, akka = true)
  def putTest(parameter: Map[String, String], req: TestModel): Result[String] = {
    assert(parameter("id") == "1")
    Result.success("OK")
  }

  @get(uri = "/test/:id/", http = true, akka = true)
  def getTest(parameter: Map[String, String]): Result[TestModel] = {
    assert(parameter("id") == "1")
    Result.success(TestModel("测试"))
  }

  @delete(uri = "/test/:id/", http = true, akka = true)
  def deleteTest(parameter: Map[String, String]): Result[String] = {
    assert(parameter("id") == "1")
    Result.success("OK")
  }
}

case class BService() {

  @post(uri = "/test/", http = true, akka = true)
  def postTest(parameter: Map[String, String], req: TestModel): Result[String] = {
    assert(req.name == "测试")
    Result.success("OK")
  }

  @put(uri = "/test/:id/", http = true, akka = true)
  def putTest(parameter: Map[String, String], req: TestModel): Result[String] = {
    assert(parameter("id") == "1")
    Result.success("OK")
  }

  @get(uri = "/test/:id/", http = true, akka = true)
  def getTest(parameter: Map[String, String]): Result[TestModel] = {
    assert(parameter("id") == "1")
    Result.success(TestModel("测试"))
  }

  @delete(uri = "/test/:id/", http = true, akka = true)
  def deleteTest(parameter: Map[String, String]): Result[String] = {
    assert(parameter("id") == "1")
    Result.success("OK")
  }
}

