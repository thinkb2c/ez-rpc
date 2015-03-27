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
1. 支持基于注解的服务注册
1. client支持异步与同步模式
1. 支持Json（推荐Json格式）与Xml

##使用

    <dependency>
        <groupId>com.ecfront</groupId>
        <artifactId>ez-rpc</artifactId>
        <version>1.9.5</version>
    </dependency>

###开启RPC服务

    RPC.server.startup().<get|post|put|delete>[<请求数据类型>](<URI>, [<请求数据类型>] , {
        (parameter, body,inject) =>
            <业务方法>
        }
    ).shutdown()
    
业务处理方法参数：
> parameter: Map[String, String] 为请求参数，来自url正则（如 /test/:id/）及path （如 /?token=1111）
> body: Any 为请求主体，只存在于POST与PUT方法中
> inject:Any 为前置执行方法返回的对象

*使用xml格式时需要setChannel(false)，<请求数据类型>为`scala.xml.Node`*

###使用注解注册服务

    RPC.server.startup().autoBuilding(<带注解的对象1>).autoBuilding(<带注解的对象...>).shutdown()

注解与方法形参要求：

* get(uri,是否使用http通道,是否使用akka通道) 对应的方法签名为 def method(parameter: Map[String, String],inject:Any): Any
* post(uri,是否使用http通道,是否使用akka通道) 对应的方法签名为 def method(parameter: Map[String, String], body: Any,inject:Any): Any
* put(uri,是否使用http通道,是否使用akka通道) 对应的方法签名为 def method(parameter: Map[String, String], body: Any,inject:Any): Any
* delete(uri,是否使用http通道,是否使用akka通道) 对应的方法签名为 def method(parameter: Map[String, String],inject:Any): Any
    
####注意
1. 确保注解方法不存在形参数量相同的方法重载
1. post与put方法两个形参如为scala内置类型则需要用全路径做为类型名，如`req scala.collection.immutable.Map[String, Any]`

*详见 AutoBuildingSpec.scala*

###访问RPC服务（异步）

    RPC.client.startup().<get|post|put|delete>[<返回数据类型>](<URI>, [请求数据对象],[<返回数据类型>], {
        result =>
            <业务方法>
        }
    )

###访问RPC服务（同步）

    RPC.client.startup().<get|post|put|delete>[<返回数据类型>](<URI>, [请求数据对象],[<返回数据类型>]):Option[Resp[<返回数据类型>])

*使用xml格式时需要setChannel(false)，<返回数据类型>为`scala.xml.Node`*

###参数设置

* 端口：`RPC.<server|client>.setPort(<端口，默认8080>)`
* 主机：`RPC.<server|client>.setHost(<主机，默认0.0.0.0>)`
* 通道（HTTP或AKKA，默认为HTTP）：`使用akka通道：RPC.<server|client>.useHighPerformance()`
* 格式化URL : `RPC.setFormatUrl(<String => String>)` 
* 前置执行方法 : `RPC.setPreExecute(<method,url,parameters => Resp[Any]>)` ，仅当返回code="200"时才会往下执行，反之直接返回结果，可用于权限认证
* 后置执行方法 :  `RPC.setPostExecute(<Any =>  Any>)` ，在主体方法执行完成后执行

##示例（更多示例见测试代码）

    val server = RPC.server.setChannel(false).startup()                                 //使用http通道，开启服务
         .get("/index/", {                                                              //注册 get:/index/
           (parameter, _) =>                                                            //parameter是url参数，_是占位符，因为get操作没有body值
             //业务操作
             Resp.success("完成")                                                     //返回Resp包装的统一对象
         }).post[String]("/index/", classOf[String], {                                  //注册 post:/index/
           (parameter, body) =>                                                         //body为请求对象
              //业务操作
             Resp.success(body)
         }).put[TestModel]("/index/:id/", classOf[TestModel], {                         //注册 put:/index/:id/ ，url可加动态参数，用:开头
           (param, body) =>
              //业务操作
             Resp.success(body)
         }).put[TestModel]("/custom/:id/", classOf[TestModel], {
           (param, body) =>
             //业务操作
             body                                                                       //返回原生方法（不用Resp包装）
         })

    val client = RPC.client.setChannel(false).startup()                                 //使用http通道，开启连接
        .get[String]("/index/", classOf[String], {                                      //获取资源
          result =>
            assert(result.code == "200")                                                //返回的状态码，详见RPC.Code
            assert(result.body == "完成")                                               //返回的是Resp包装对象，body属性为实际内容
        }).post[String]("/index/", "测试", classOf[String], {                           //添加资源
          result =>
            assert(result.code == "200")
            assert(result.body == "测试")
        }).put[TestModel]("/index/test/", TestModel("测试"), classOf[TestModel], {      //更新资源，资源可为自定义对象
          result =>
            assert(result.code == "200")
            assert(result.body.name == "测试")
        }).put[TestModel]("/index/test/", TestModel("测试"))                            //更新资源，不需要回调操作

    client.raw.put[TestModel]("/custom/test/", TestModel("测试"), classOf[TestModel], { //raw 表示操作返回值为原生对象
          result =>
            assert(result.name == "测试")                                               //此时返回值为原生对象（没有Resp包装）
        })

    assert(client.getSync[Boolean]("/boolean/", classOf[Boolean]).get.body)

    server.shutdown()                                                                   //关闭服务

    case class TestModel(name: String)

=======================================================


### Check out sources
`git clone https://github.com/gudaoxuri/ez-rpc.git`

### License

Under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0


