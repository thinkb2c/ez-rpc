package com.ecfront.rpc.html

import java.net.URLEncoder

import com.ecfront.rpc.http._
import com.ecfront.rpc.http.client.HttpClient
import com.ecfront.rpc.http.server.HttpServer
import io.netty.handler.codec.http.Cookie
import org.scalatest.FunSuite

class HttpSpec extends FunSuite {

  test("http服务及客户端测试") {

    HttpServer.startup(3000)

    Register.get("/user/", new SimpleFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): HttpResult[_] = {
        HttpResult.success[String](parameters.get("arg").get)
      }
    })

    Register.post("/user/", new Fun[Person](classOf[Person]) {
      override def execute(parameters: Map[String, String], body: Person, cookies: Set[Cookie]): HttpResult[_] = {
        HttpResult.success[Person](body)
      }
    })

    Register.put("/user/a/", new Fun[Person](classOf[Person]) {
      override def execute(parameters: Map[String, String], body: Person, cookies: Set[Cookie]): HttpResult[_] = {
        body.address.addr="modify"
        HttpResult.success[Person](body)
      }
    })

    Register.delete("/user/a/", new SimpleFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): HttpResult[_] = {
        HttpResult.success[String]("成功")
      }
    })

    Register.get("/user/:id/:addr/post/", new SimpleFun {
      override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): HttpResult[_] = {
        HttpResult.success[Map[String, String]](parameters)
      }
    })

    val client = new HttpClient

    val result1 = client.get("http://127.0.0.1:3000/user/?arg=测试", classOf[String])
    assert(result1.body == "测试")
    val result2 = client.post("http://127.0.0.1:3000/user/", Person("孤岛旭日",Address("HangZhou")), classOf[Person])
    assert(result2.body.name == "孤岛旭日")
    assert(result2.body.address.addr == "HangZhou")
    val result3 = client.put("http://127.0.0.1:3000/user/a/", Person("sunisle",Address("HangZhou")), classOf[Person])
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
    assert(result6.code == HttpCode.BAD_REQUEST.toString)

    HttpServer.destroy
  }
}
