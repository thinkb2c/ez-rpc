package com.ecfront.rpc

import java.util.concurrent.CountDownLatch

import com.ecfront.rpc.RPC.Result
import org.scalatest.FunSuite

class FunSpec extends FunSuite {

  test("功能测试") {
    jsonFunTest(highPerformance = false)
    jsonFunTest(highPerformance = true)
    xmlFunTest()
  }

  def jsonFunTest(highPerformance: Boolean) {
    val latch = new CountDownLatch(6)

    val server = RPC.server.setPort(808)
    if (highPerformance) server.useHighPerformance()
    server.startup()
      .get("/number/", {
      (param, _) =>
        Result.success(1L)
    }).get("/boolean/", {
      (param, _) =>
        Result.success(true)
    }).get("/index/", {
      (param, _) =>
        assert(param("a") == "1")
        Result.success("完成")
    }).post[String]("/index/", classOf[String], {
      (param, body) =>
        Result.success(body)
    }).put[TestModel]("/index/:id/", classOf[TestModel], {
      (param, body) =>
        assert(body.name == "测试")
        assert(param.get("id").get == "test")
        Result.success(body)
    }).put[TestModel]("/custom/:id/", classOf[TestModel], {
      (param, body) =>
        assert(body.name == "测试")
        assert(param.get("id").get == "test")
        //Result custom type
        body
    })

    /* httpServer.upload("/upload/", {
       (param, files, cookie) =>
         Result.success(files)
     })*/

    val client = RPC.client.setPort(808)
    if (highPerformance) client.useHighPerformance()
    client.startup()
      .get[Long]("/number/", classOf[Long], {
      result =>
        assert(result.code == "200")
        assert(result.body == 1L)
        latch.countDown()
    }).get[Boolean]("/boolean/", classOf[Boolean], {
      result =>
        assert(result.code == "200")
        assert(result.body)
        latch.countDown()
    }).get[String]("/index/?a=1", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "完成")
        latch.countDown()
    }).post[String]("/index/", "测试", classOf[String], {
      result =>
        assert(result.code == "200")
        assert(result.body == "测试")
        latch.countDown()
    }).put[TestModel]("/index/test/", TestModel("测试"), classOf[TestModel], {
      result =>
        assert(result.code == "200")
        assert(result.body.name == "测试")
        latch.countDown()
    }).put[TestModel]("/index/test/", TestModel("测试"))

    //raw json
    client.raw.put[TestModel]("/custom/test/", TestModel("测试"), classOf[TestModel], {
      result =>
        assert(result.name == "测试")
        latch.countDown()
    })

    latch.await()

    assert(client.getSync[Long]("/number/", classOf[Long]).get.body == 1L)
    assert(client.getSync[Boolean]("/boolean/", classOf[Boolean]).get.body)
    assert(client.getSync[String]("/index/?a=1", classOf[String]).get.body == "完成")
    assert(client.postSync[String]("/index/", "测试", classOf[String]).get.body == "测试")
    assert(client.putSync[TestModel]("/index/test/", TestModel("测试"), classOf[TestModel]).get.body.name == "测试")
    client.putSync[TestModel]("/index/test/", TestModel("测试"))
    assert(client.raw.putSync[TestModel]("/custom/test/", TestModel("测试"), classOf[TestModel]).get.name == "测试")

    server.shutdown()
  }

  def xmlFunTest(): Unit = {
    val server = RPC.server.setPort(3001).startup()
      .put[scala.xml.Node]("/custom/:id/", classOf[scala.xml.Node], {
      (param, body) =>
        assert((body \ "city").size != 0)
        body
    })
    //raw xml must channel=false and request class = scala.xml.Node
    val latch = new CountDownLatch(1)
    val xmlClient = RPC.client.setPort(3001).startup().raw
    xmlClient.get[scala.xml.Node]("http://flash.weather.com.cn:80/wmaps/xml/china.xml", classOf[scala.xml.Node], {
      result =>
        assert((result \ "city").size != 0)
        xmlClient.put[scala.xml.Node]("/custom/test/", result, classOf[scala.xml.Node], {
          result2 =>
            assert(result2.toString() == result.toString())
            latch.countDown()
        })
    })
    latch.await()
    server.shutdown()
  }
}


case class TestModel(name: String)

