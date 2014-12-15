package com.ecfront.rpc.socket

import com.ecfront.rpc.RPC
import com.ecfront.rpc.RPC.Result
import com.ecfront.rpc.socket.client.SocketClientFun
import com.ecfront.rpc.socket.server.SocketServerFun
import org.scalatest.FunSuite

class SocketSpec extends FunSuite {

  test("Socket服务及客户端测试") {

    val server = RPC.Server.socket(3001).process(new SocketServerFun(classOf[String]) {
      override def execute(body: String): Result[_] = {
        RPC.Result.success[String]("收到")
      }
    })

    RPC.Client.socket(3001).send("第一次发送").reply(new SocketClientFun(classOf[String]) {
      override def execute(code: String, body: String, message: String): Any = {
        assert(body == "收到")
        assert(code == "200")
      }
    }).startup()

    RPC.Client.socket(3001).send("第二次发送").reply(new SocketClientFun(classOf[String]) {
      override def execute(code: String, body: String, message: String): Any = {
        assert(body == "收到")
        assert(code == "200")
      }
    }).startup()

    server.destroy()

    /*RPC.Server.socket(3001).process(new SocketServerFun(classOf[Person]) {
      override def execute(person: Person): Result[_] = {
        assert(person.address.addr == "杭州")
        person.name = "modify"
        RPC.Result.success[Person](person)
      }
    })

    RPC.Client.socket(3001).send(Person("孤岛旭日", Address("杭州"))).reply(new SocketClientFun(classOf[Person]) {
      override def execute(code: String, person: Person, message: String): Any = {
        assert(person.name == "modify")
        assert(code == "200")
      }
    }).startup()*/

    /*    RPC.Client.socket(8181).send("local2ftp").reply(new SocketClientFun(classOf[String]) {
          override def execute(code: String, person: String, message: String): Any = {
            assert(code == "200")
          }
        }).startup()*/

  }
}
