import com.ecfront.rpc.http.client.HttpClient
import com.ecfront.rpc.http.server.HttpServer
import com.ecfront.rpc.http.{Fun, HttpResult, Register, SimpleFun}
import io.netty.handler.codec.http.Cookie
import org.scalatest._

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

    HttpServer.destroy
  }
}

case class Person(var name: String,var address:Address)
case class Address(var addr: String)
