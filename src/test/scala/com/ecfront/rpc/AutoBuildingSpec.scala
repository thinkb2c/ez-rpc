package com.ecfront.rpc

import com.ecfront.common.Resp
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
    server.startup().autoBuilding(AService)

    val client = RPC.client.setPort(808)
    if (highPerformance) client.useHighPerformance()
    client.startup()

    assert(client.getSync[TestModel]("/test1/1/", classOf[TestModel]).get.body.name == "测试")
    assert(client.postSync[String]("/test1/", TestModel("测试"), classOf[String]).get.body == "OK")
    assert(client.putSync[String]("/test1/1/", TestModel("测试"), classOf[String]).get.body == "OK")
    assert(client.deleteSync[String]("/test1/1/", classOf[String]).get.body == "OK")

    server.shutdown()

    val server2 = RPC.server.setPort(8088)
    if (highPerformance) server2.useHighPerformance()
    server2.setFormatUrl("/base" + _).setPreExecute({
      (method, url, parameters) =>
        if (method == Method.PUT && url == "/base/test2/:id/") {
          Resp.unAuthorized("认证失败！")
        } else {
          Resp.success(User("1", "admin"))
        }
    }).startup().autoBuilding(BService())

    val client2 = RPC.client.setPort(8088)
    if (highPerformance) client2.useHighPerformance()
    client2.startup()

    assert(client2.getSync[TestModel]("/base/test2/1/", classOf[TestModel]).get.body.name == "测试")
    assert(client2.postSync[String]("/base/test2/", TestModel("测试"), classOf[String]).get.body == "OK")
    assert(client2.putSync[String]("/base/test2/1/", TestModel("测试"), classOf[String]).get.message == "认证失败！")
    assert(client2.deleteSync[String]("/base/test2/1/", classOf[String]).get.body == "OK")

    server2.shutdown()

  }

}


object AService {

  @post(uri = "/test1/", http = true, akka = true)
  def postTest(parameter: Map[String, String], req: TestModel, inject: Any): Resp[String] = {
    assert(req.name == "测试")
    Resp.success("OK")
  }

  @put(uri = "/test1/:id/", http = true, akka = true)
  def putTest(parameter: Map[String, String], req: TestModel, inject: Any): Resp[String] = {
    assert(parameter("id") == "1")
    Resp.success("OK")
  }

  @get(uri = "/test1/:id/", http = true, akka = true)
  def getTest(parameter: Map[String, String], inject: Any): Resp[TestModel] = {
    assert(parameter("id") == "1")
    Resp.success(TestModel("测试"))
  }

  @delete(uri = "/test1/:id/", http = true, akka = true)
  def deleteTest(parameter: Map[String, String], inject: Any): Resp[String] = {
    assert(parameter("id") == "1")
    Resp.success("OK")
  }
}

case class BService() {

  @post(uri = "/test2/", http = true, akka = true)
  def postTest(parameter: Map[String, String], req: TestModel, inject: User): Resp[String] = {
    assert(req.name == "测试")
    assert(inject.id == "1")
    assert(inject.name == "admin")
    Resp.success("OK")
  }

  @put(uri = "/test2/:id/", http = true, akka = true)
  def putTest(parameter: Map[String, String], req: TestModel, inject: User): Resp[String] = {
    //不应该进入
    assert(1 == 2)
    Resp.success("OK")
  }

  @get(uri = "/test2/:id/", http = true, akka = true)
  def getTest(parameter: Map[String, String], inject: User): Resp[TestModel] = {
    assert(parameter("id") == "1")
    assert(inject.id == "1")
    assert(inject.name == "admin")
    Resp.success(TestModel("测试"))
  }

  @delete(uri = "/test2/:id/", http = true, akka = true)
  def deleteTest(parameter: Map[String, String], inject: User): Resp[String] = {
    assert(parameter("id") == "1")
    assert(inject.id == "1")
    assert(inject.name == "admin")
    Resp.success("OK")
  }
}

case class User(id: String, name: String)
