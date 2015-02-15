EZ RPC
===
### 简洁易用的RPC服务，支持Restful HTTP、Akka

 =======================================================

##使用

    <dependency>
        <groupId>com.ecfront</groupId>
        <artifactId>rpc</artifactId>
        <version>0.9</version>
    </dependency>

###HTTP服务

    RPC.Server.http(<Port>,[<Host>]).<get|post|put|delete|upload>("<URI>", [<请求数据类型>] , {
        (param, body, cookie) =>
              <业务方法>
        })

###HTTP异步请求

    RPC.Client.http.<get|post|put|delete>Async[<返回数据类型>](<Url>, classOf[<返回数据类型>], {
          result =>
            <业务方法>
        })

###Akka服务

    RPC.Server.akka(<Port>,[<Host>]) .process({
          request =>
             <业务方法>
        })

###Akka请求

    RPC.Client.akka(<Port>,[<Host>]).process[<返回数据类型>](AkkaRequest("<action>", <数据对象>), {
         result =>
              <业务方法>
          })

##示例（更多示例见测试代码）

    //启动HTTP服务
     val httpServer = RPC.Server.http(3000)
    //注册一个GET方法
    httpServer.get("/index/", {
      (param, cookie) =>
        Result.success("完成")
    })
    //注册一个PUT方法，处理TestModel类型
    httpServer.put[TestModel]("/index/:id/", classOf[TestModel], {
      (param, body, cookie) =>
        assert(param.get("id") == "test")
        Result.success(body)
    })
    //启动HTTP请求
     val client = RPC.Client.http
     httpClient.getAsync[String]("http://127.0.0.1:3000/index/", classOf[String], {
       result =>
         assert(result.code == "200")
         assert(result.body == "完成")
         latch.countDown()
     })
    httpClient.putAsync[TestModel]("http://127.0.0.1:3000/index/test/", TestModel("测试"), classOf[TestModel], {
      result =>
        assert(result.code == "200")
        assert(result.body.name == "测试")
        latch.countDown()
    })

    //注册Akka服务
    RPC.Server.akka(3000).process({
           req =>
             Result.success(req.body)
         })
    //发起Akka请求
    RPC.Client.akka(3000).process[TestModel](AkkaRequest("get", TestModel("测试")), {
          result =>
            assert(result.code == "200")
            assert(result.body.name == "测试")
            latch.countDown()
        })

    case class TestModel(name: String)

=======================================================


### Check out sources
`git clone https://github.com/gudaoxuri/ez-rpc.git`

### License

Under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0

