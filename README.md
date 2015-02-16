EZ RPC
===
### 简洁易用的Restful RPC服务，支持 HTTP、Akka

 =======================================================

##功能

1. 支持HTTP及Akka两种通道，前者网络穿透性高，后者性能高（@see PerformanceSpec）
1. 统一HTTP及Akka的调用，实现方式对操作透明
1. Restful设计，所有业务都抽象成对资源的操作
1. 支持启动多个RPC服务（@see MultiSpec）
1. 类库形式，侵入性低，集成友好
1. 支持链式编程风格，使用方便

##使用

    <dependency>
        <groupId>com.ecfront</groupId>
        <artifactId>rpc</artifactId>
        <version>1.0</version>
    </dependency>

###开启RPC服务

    RPC.server.startup().<get|post|put|delete>[<请求数据类型>](<URI>, [<请求数据类型>] , {
        (parameter, body) =>
            <业务方法>
        }
    ).shutdown()

###访问RPC服务

    RPC.client.startup().<get|post|put|delete>[<返回数据类型>](<URI>, [请求数据对象],[<返回数据类型>], {
        result =>
            <业务方法>
        }
    )

###参数设置

*  端口：`RPC.<server|client>.setPort(<端口，默认8080>)`
*  主机：`RPC.<server|client>.setHost(<主机，默认0.0.0.0>)`
*  通道（HTTP或Akka）：`RPC.<server|client>.setChannel(<true|false，true为Akka通道，false为HTTP通道，默认为true>)`


##示例（更多示例见测试代码）

    val server = RPC.server.setChannel(false).startup()                                          //使用http通道，开启服务
         .get("/index/", {                                                                       //注册 get:/index/
           (parameter, _) =>                                                                     //parameter是url参数，_是占位符，因为get操作没有body值
             //业务操作
             Result.success("完成")                                                              //返回Result包装的统一对象
         }).post[String]("/index/", classOf[String], {                                           //注册 post:/index/
           (parameter, body) =>                                                                  //body为请求对象
              //业务操作
             Result.success(body)
         }).put[TestModel]("/index/:id/", classOf[TestModel], {                                  //注册 put:/index/:id/ ，url可加动态参数，用:开头
           (param, body) =>
              //业务操作
             Result.success(body)
         }).put[TestModel]("/custom/:id/", classOf[TestModel], {
           (param, body) =>
             //业务操作
             body                                                                                //返回原生方法（不用Result包装）
         })

    val client = RPC.client.setChannel(false).startup()                                          //使用http通道，开启连接
        .get[String]("/index/", classOf[String], {                                               //获取资源
          result =>
            assert(result.code == "200")                                                         //返回的状态码，详见RPC.Code
            assert(result.body == "完成")                                                        //返回的是Result包装对象，body属性为实际内容
        }).post[String]("/index/", "测试", classOf[String], {                                    //添加资源
          result =>
            assert(result.code == "200")
            assert(result.body == "测试")
        }).put[TestModel]("/index/test/", TestModel("测试"), classOf[TestModel], {               //更新资源，资源可为自定义对象
          result =>
            assert(result.code == "200")
            assert(result.body.name == "测试")
        }).put[TestModel]("/index/test/", TestModel("测试"))                                     //更新资源，不需要回调操作

    client.raw.put[TestModel]("/custom/test/", TestModel("测试"), classOf[TestModel], {          //raw 表示操作返回值为原生对象
          result =>
            assert(result.name == "测试")                                                        //此时返回值为原生对象（没有Result包装）
        })

    server.shutdown()                                                                            //关闭服务

    case class TestModel(name: String)

=======================================================


### Check out sources
`git clone https://github.com/gudaoxuri/ez-rpc.git`

### License

Under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0


