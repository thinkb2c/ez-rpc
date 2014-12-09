package com.ecfront.rpc.http

import java.net.URLEncoder

import com.ecfront.rpc.http.server.{HttpFun, SimpleHttpFun}
import com.ecfront.rpc.{Address, Person, RPC}
import io.netty.handler.codec.http.Cookie
import org.scalatest.FunSuite

class HttpSpec extends FunSuite {

  test("HTTP服务及客户端测试") {

    val httpServer = RPC.Server.http(3000)

    httpServer.get("/user/", new SimpleHttpFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): RPC.Result[_] = {
        RPC.Result.success[String](parameters.get("arg").get)
      }
    })

    httpServer.post("/user/", new HttpFun(classOf[Person]) {
      override def execute(parameters: Map[String, String], body: Person, cookies: Set[Cookie]): RPC.Result[_] = {
        RPC.Result.success[Person](body)
      }
    })

    httpServer.put("/user/a/", new HttpFun(classOf[Person]) {
      override def execute(parameters: Map[String, String], body: Person, cookies: Set[Cookie]): RPC.Result[_] = {
        body.address.addr = "modify"
        RPC.Result.success[Person](body)
      }
    })

    httpServer.delete("/user/a/", new SimpleHttpFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): RPC.Result[_] = {
        RPC.Result.success[String]("成功")
      }
    })

    httpServer.get("/user/:id/:addr/post/", new SimpleHttpFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): RPC.Result[_] = {
        RPC.Result.success[Map[String, String]](parameters)
      }
    })

    val client = RPC.Client.http

    val result1 = client.get("http://127.0.0.1:3000/user/?arg=测试", classOf[String])
    assert(result1.body == "测试")
    val result2 = client.post("http://127.0.0.1:3000/user/", Person("孤岛旭日", Address("HangZhou")), classOf[Person])
    assert(result2.body.name == "孤岛旭日")
    assert(result2.body.address.addr == "HangZhou")
    val result3 = client.put("http://127.0.0.1:3000/user/a/", Person("sunisle", Address("HangZhou")), classOf[Person])
    assert(result3.body.name == "sunisle")
    assert(result3.body.address.addr == "modify")
    val result4 = client.delete("http://127.0.0.1:3000/user/a/", classOf[String])
    assert(result4.code == "200")

    //Regex
    val result5 = client.get("http://127.0.0.1:3000/user/100/杭州/post/?arg=测试", classOf[Map[String, String]])
    assert(result5.body.get("id").get == "100")
    assert(result5.body.get("addr").get == URLEncoder.encode("杭州", "UTF-8"))
    assert(result5.body.get("arg").get == "测试")
    val result6 = client.get("http://127.0.0.1:3000/user/100/杭州/?arg=测试", classOf[Map[String, String]])
    assert(result6.code == RPC.Result.Code.BAD_REQUEST.toString)

    httpServer.destroy
  }
}
