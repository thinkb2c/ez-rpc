EZ RPC
===
### 基于netty的RPC封装，支持Restful HTTP、Socket

 =======================================================

##使用

    <dependency>
        <groupId>com.ecfront</groupId>
        <artifactId>rpc</artifactId>
        <version>0.5</version>
    </dependency>

###HTTP服务

    RPC.Server.http(<Port>).<get|post|put|delete>("<URI>", <业务方法>).destroy()

###HTTP请求

    //初始一个客户端实例
    val client = new HttpClient
    //发起一个请求，返回HttpResult对象，result.code为200表示成功，反之为失败
    val result = client.<get|post|put|delete>("<URI>", classOf[<返回对象类型>])

###Socket服务

    RPC.Server.socket(<Port>) .process(<业务方法>).destroy()

###Socket请求

    RPC.Client.socket(<Port>).send(<发送内容>).reply(<回复方法>).startup()

##示例（更多示例见测试代码）

    //启动HTTP服务
     val httpServer = RPC.Server.http(3000)
    //注册一个GET方法
     httpServer.get("/user/:id/", new SimpleHttpFun {
           override def execute(parameters: Map[String, String], body: String, cookies: Set[Cookie]): RPC.Result[_] = {
             //返回成功消息
             RPC.Result.success[Map[String, String]](parameters)
           }
     })
    //注册一个POST方法
     httpServer.post("/user/", new HttpFun(classOf[Person]) {
           override def execute(parameters: Map[String, String], body: Person, cookies: Set[Cookie]): RPC.Result[_] = {
            //返回成功消息
             RPC.Result.success[Person](body)
           }
     })
    //启动HTTP请求
     val client = RPC.Client.http
     val result1 = client.get("http://127.0.0.1:3000/user/100/?arg=测试", classOf[String])
     val result2 = client.post("http://127.0.0.1:3000/user/", Person("孤岛旭日",Address("HangZhou")), classOf[Person])
    //关闭HTTP服务
    httpServer.destroy

    //注册Socket服务
    RPC.Server.socket(3001).process(new SocketServerFun(classOf[Person]) {
           override def execute(person: Person): Result[_] = {
             assert(person.address.addr == "杭州")
             person.name = "modify"
             RPC.Result.success[Person](person)
           }
    })
    //发起Socket请求
    RPC.Client.socket(3001).send(Person("孤岛旭日", Address("杭州"))).reply(new SocketClientFun(classOf[Person]) {
           override def execute(code: String, person: Person, message: String): Any = {
             assert(person.name == "modify")
             assert(code == "200")
           }
    }).startup()


     case class Person(var name: String,var address:Address)
     case class Address(var addr: String)

=======================================================


### Check out sources
`git clone https://github.com/gudaoxuri/ez-rpc.git`

### License

Under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0

