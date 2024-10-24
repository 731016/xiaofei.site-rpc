## RPC框架实现思路

RPC：远程过程调用，简化调用



编程导航：https://www.codefather.cn/course/1768543954720022530

代码地址：https://github.com/731016/xiaofei.site-rpc

### 基本设计



消费者需要调用提供者，需要提供者启动一个`web服务`，通过`请求客户端`发送http或其他协议的请求来调用



可以提供一个统一的服务调用接口，通过`请求处理器`，根据客户端的请求参数来进行不同的处理、调用不同的服务和方法



维护一个`本地服务注册器`，记录服务和对应实现类的映射

```java
调用orderService服务的order,方法，可设置参数为service=orderService,medthod=order
请求处理器找到服务注册器中对应的服务实现类，并通过java反射机制调用method指定方法
```

> java对象无法直接在网络中传输，需要通过`序列化`和`反序列化`



为简化消费者发起请求的代码，可通过代理模式，为消费者需要调用的接口生成一个代理对象，由代理对象完成请求和响应

![image-20241016222934422](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241016222934422.png)

### 扩展设计

#### 服务注册与发现

> 消费者需要知道服务提供者的调用地址？

需要`注册中心`保持服务提供者的地址，需要调用时，从注册中心获取



![image-20241016224559125](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241016224559125.png)

使用Redis、zookeeper、nacos



#### 负载均衡

> 如果存在多个服务提供者，消费者应该调用哪个服务提供者？

通过指定不同的算法来决定调用哪个服务提供者，比如轮询、随机、根据性能动态调用

![image-20241016225018742](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241016225018742.png)

#### 容错机制

> 如果服务服务调用失败，怎么处理？

保证分布式系统的高可用，通常会增加容错机制，如失败重试、降级调用其它接口...

![image-20241016230806725](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241016230806725.png)

#### 其它

+ 服务提供着下线了怎么办？需要一个失效节点剔除机制

+ 消费者每次都从注册中心拉取信息，性能是否会变差？使用缓存优化
+ 优化RPC框架的传输通信性能？选择合适的网络框架、自定义协议头、节约传输体积
+ 保持可扩展性？使用Java的SPI机制、配置化



## 开发部署（简易版）



![image-20241019000313646](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019000313646.png)

### 准备项目

#### 1.初始化

准备

```java
xiaofei.site-rpc maven项目 父级maven项目，存储公共依赖
rpc-common	公共模块，接口、model
rpc-consumer消费者，使用远程服务
rpc-provider服务提供者，接口具体实现
rpc-system rpc框架
```

#### 2.公共模块

公共模块需要被`消费者`和`服务提供者`引用



整体结构

![image-20241019001042414](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019001042414.png)

（1）编写实体类User

```java
package site.xiaofei.common.model;

import java.io.Serializable;

/**
 * @author tuaofei
 * @description 用户
 * @date 2024/10/17
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = -5571881012294210153L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

> 注意：实现Serializable接口，为后续网络传输序列化提供支持

> serialVersionUID适用于java序列化机制。简单来说，JAVA序列化的机制是通过 判断类的serialVersionUID来验证的版本一致的。在进行反序列化时，JVM会把传来的字节流中的serialVersionUID于本地相应实体类的serialVersionUID进行比较。如果相同说明是一致的，可以进行反序列化，否则会出现反序列化版本一致的异常，即是InvalidCastException。

**具体序列化的过程是这样的：**序列化操作时会把系统当前类的serialVersionUID写入到序列化文件中，当反序列化时系统会自动检测文件中的serialVersionUID，判断它是否与当前类中的serialVersionUID一致。如果一致说明序列化文件的版本与当前类的版本是一样的，可以反序列化成功，否则就失败



(2)编写用户服务接口UserService,提供一个获取用户的方法

```java
package site.xiaofei.common.service;

import site.xiaofei.common.model.User;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/17
 */
public interface UserService {

    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);
}
```



#### 3.xiaofei.site-rpc父级maven项目

（1）pom.xml

准备公共依赖，统一依赖版本，子模块需要用到的地方不用再自己定义版本

```xml
<properties>
        <hutool-version>5.8.16</hutool-version>
        <lombok-version>1.18.30</lombok-version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
                <version>${hutool-version}</version>
            </dependency>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok-version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
```





#### 4.服务提供者

真正实现接口的模块



（1）pom.xml

```xml
<dependencies>
        <dependency>
            <groupId>xiaofei.site</groupId>
            <artifactId>rpc-system</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>xiaofei.site</groupId>
            <artifactId>rpc-common</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
```

（2）编写服务实现类，实现公共模块中定义的用户服务接口

```java
package site.xiaofei.provider;

import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;

/**
 * @author tuaofei
 * @description 服务提供者
 * @date 2024/10/17
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        if (user == null){
            return null;
        }
        System.out.println(String.format("用户名：%s",user.getName()));
        return user;
    }
}
```

（3）编写服务提供者启动类RpcProviderExample，提供启动服务提供者的方法

```java
package site.xiaofei.provider;

import site.xiaofei.common.service.UserService;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.server.HttpServer;
import site.xiaofei.server.VertxServer;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/17
 */
public class RpcProviderExample {

    public static void main(String[] args) {
        //提供服务
    }
}
```

服务提供者模块目录

![image-20241019002554772](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019002554772.png)

#### 5.服务消费者

需要调用服务的模块

（1）pom.xml

```xml
<dependencies>
        <dependency>
            <groupId>xiaofei.site</groupId>
            <artifactId>rpc-system</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>xiaofei.site</groupId>
            <artifactId>rpc-common</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
```



(2)创建消费者启动类RpcConsumerExample，编写调用接口的代码

```java
package site.xiaofei.consumer;

import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;

/**
 * @author tuaofei
 * @description 消费者
 * @date 2024/10/17
 */
public class RpcConsumerExample {

    public static void main(String[] args) {
        //todo 需要获取UserService实现类的对象
        UserService userService = null;

        User user = new User();
        user.setName("土澳菲");
        User resultUser = userService.getUser(user);
        if (resultUser != null){
            System.out.println(resultUser.getName());
        }else{
            System.out.println("user is null!");
        }
    }
}
```

现在肯定无法获取，先预留null；后续通过rpc框架，获取一个支持远程调用服务提供者的代理对象，快速调用



服务消费者目录结构

![image-20241019003131124](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019003131124.png)



### web服务器

服务提供者需要提供远程服务，必须要一个web服务器，能够接收、处理、响应请求



选择有很多，比如tomcat、NIO框架Netty、Vert.x



这里选择Vert.x，官方文档：[Eclipse Vert.x (vertx.io)](https://vertx.io/)



（1）打开rpc-system模块，引入依赖

```xml
<dependencies>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-core</artifactId>
            <version>4.5.1</version>
        </dependency>
    </dependencies>
```

（1）编写一个web服务器的接口HttpServer，定义统一的启动服务器方法，便于后续扩展，可实现不同web服务器

```java
package site.xiaofei.server;

/**
 * @author tuaofei
 * @description Http服务器接口
 * @date 2024/10/17
 */
public interface HttpServer {

    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}
```

（3）编写基于vert.x实现的web服务器VertxServer，能够监听指定端口并处理请求

```java
package site.xiaofei.server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

/**
 * @author tuaofei
 * @description 基于vertx实现的web服务器
 * @date 2024/10/17
 */
public class VertxServer implements HttpServer {
    @Override
    public void doStart(int port) {
        //创建vertx实例
        Vertx vertx = Vertx.vertx(vertxOptions);

        //创建http服务器
        io.vertx.core.http.HttpServer vertxHttpServer = vertx.createHttpServer();

        //监听端口并处理请求
        vertxHttpServer.requestHandler(request -> {
            //处理http请求
            String.format("receive request ：%s %s", request.uri(), request.method());
            //发送http响应
            request.response()
                    .putHeader("content-type", "text/plain")
                    .end("hello from vert.x http server!");
        });

        //启动http服务器并监听指定端口
        vertxHttpServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println(String.format("server is now listening on port %s", port));
            } else {
                System.out.println(String.format("failed to start server %s", result.cause()));
            }
        });
    }
}
```



（4）验证web服务器能否启动成功并接收请求

修改服务提供者模块的RpcProviderExample，编写启动web服务的代码

```java
package site.xiaofei.provider;

import site.xiaofei.common.service.UserService;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.server.HttpServer;
import site.xiaofei.server.VertxServer;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/17
 */
public class RpcProviderExample {

    public static void main(String[] args) {
        //启动http服务
        HttpServer vertxServer = new VertxServer();
        vertxServer.doStart(8080);
    }
}
```

通过浏览器访问`localhost:8080`,查看能否正常访问，并输出文字hello from vert.x http server!



rpc模块目录结构

![image-20241019004517385](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019004517385.png)

### 本地服务注册

暂时不使用第三方注册中心，直接把服务提供者注册到本地

创建本地服务注册器LocalRegistry



使用线程安全的ConcurrentHashMap存储服务注册信息，key为服务名称（此对象表示的类或接口的名称XXX.class.getName()）、value为服务实现类(XXX.class)，之后根据要调用的服务名称获取到对应的实现类，通过反射执行



目录结构

![image-20241019004735188](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019004735188.png)

服务提供者启动时，需要把服务提供者注册到本地服务注册器，修改RpcProviderExample

```java
package site.xiaofei.provider;

import site.xiaofei.common.service.UserService;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.server.HttpServer;
import site.xiaofei.server.VertxServer;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/17
 */
public class RpcProviderExample {

    public static void main(String[] args) {

        //注册服务
        LocalRegistry.register(UserService.class.getName(),UserServiceImpl.class);

        //启动http服务
        HttpServer vertxServer = new VertxServer();
        vertxServer.doStart(8080);
    }
}
```

### 序列化器

本地服务注册后，就可根据请求信息取出实现类并调用方法了



在调用之前，需要使用到序列化器；

在请求和响应，传输参数时，需要进行对象的序列化和反序列化



>  什么是序列化和反序列化？
>
> 序列化：将java对象转换为可传输的字节数组
>
> 反序列化：将字节数组转换为java对象

序列化方式

Java原生序列化、JSON、Hessian、Kryo、protobuf



此处使用Java原生序列化



（1）在rpc-system模块中，编写序列化接口

```java
package site.xiaofei.serializer;

import java.io.IOException;

/**
 * @author xiaofei
 * @description 序列化接口
 * @date 2024/10/17
 */
public interface Serializer {

    /**
     * 序列化
     * @param object
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> byte[] serializer(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T deserializer(byte[] bytes,Class<T> type) throws IOException;
}
```

(2)基于java自带的序列化器实现JdkSerializer

```java
package site.xiaofei.serializer;

import java.io.*;

/**
 * @author tuaofei
 * @description Jdk序列化器
 * @date 2024/10/17
 */
public class JdkSerializer implements Serializer {
    @Override
    public <T> byte[] serializer(T object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        try {
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }finally {
            objectInputStream.close();
        }
    }
}
```

目录结构

![image-20241019010023080](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019010023080.png)

### 请求处理器

提供者处理调用



实现RPC的关键，作用：处理接收到的请求，并根据请求参数找到对应的服务和方法，通过反射调用，封装返回结果并响应请求



（1）请求和响应类

目录结构

![image-20241019010257867](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019010257867.png)

请求类RpcRequest的作用：封装调用需要的信息

```
反射机制必需的参数
服务名称
方法名称
调用参数的类型列表
参数列表
```

```java
package site.xiaofei.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author tuaofei
 * @description Rpc请求
 * @date 2024/10/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型列表
     */
    private Class<?>[] paramTypes;

    /**
     * 参数列表
     */
    private Object[] args;

}
```

响应类RpcResponse作用：封装调用方法得到的返回值、已经调用的信息（比如异常）

```java
package site.xiaofei.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author tuaofei
 * @description Rpc响应
 * @date 2024/10/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 响应数据类型（预留）
     */
    private Class<?> dataType;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 异常信息
     */
    private Exception exception;
}
```

(2)编写请求处理器HttpServerHandler

![image-20241019010730506](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019010730506.png)

业务流程：

```
1、反序列化请求为对象，并从请求对象获取参数
2、根据服务名称从本地注册器中获取对应的服务实例
3、通过反射调用方法得到结果
4、对结构进行封装和序列化，并写到响应中
```

```java
package site.xiaofei.server;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.serializer.JdkSerializer;
import site.xiaofei.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author tuaofei
 * @description Http请求处理器
 * 1、反序列化请求为对象，并从请求对象获取参数
 * 2、根据服务名称从本地注册器中获取对应的服务实例
 * 3、通过反射调用方法得到结果
 * 4、对结构进行封装和序列化，并写到响应中
 * @date 2024/10/17
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {


    @Override
    public void handle(HttpServerRequest request) {
        //指定序列化器
        JdkSerializer serializer = new JdkSerializer();

        //记录日志
        System.out.println(String.format("receive request ：%s %s", request.method(), request.uri()));

        //异步处理请求
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserializer(bytes, RpcRequest.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //构造响应结果
            RpcResponse rpcResponse = new RpcResponse();
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(request, rpcResponse, serializer);
                return;
            }
            //获取服务实例，反射调用
            Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
            try {
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());

                //封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("success");
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            //响应
            doResponse(request, rpcResponse, serializer);
        });


    }

    /**
     * 响应
     *
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    public void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            byte[] serialized = serializer.serializer(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
```

> 不同web服务器的请求处理器实现方式不同
>
> Vert.x通过实现Handler<HttpServerRequest>接口来自定义请求处理器
>
> 通过request.bodyHandler异步处理请求



（3）给HttpServer绑定请求处理器

修改VertxServer，通过vertxHttpServer.requestHandler绑定

```java
package site.xiaofei.server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

/**
 * @author tuaofei
 * @description 基于vertx实现的web服务器
 * @date 2024/10/17
 */
public class VertxServer implements HttpServer {
    @Override
    public void doStart(int port) {
        //创建vertx实例
        Vertx vertx = Vertx.vertx(vertxOptions);

        //创建http服务器
        io.vertx.core.http.HttpServer vertxHttpServer = vertx.createHttpServer();

        //监听端口并处理请求
        vertxHttpServer.requestHandler(new HttpServerHandler());

        //启动http服务器并监听指定端口
        vertxHttpServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println(String.format("server is now listening on port %s", port));
            } else {
                System.out.println(String.format("failed to start server %s", result.cause()));
            }
        });
    }
}
```



至此，引入了RPC框架的服务提供者模块，已经能接收请求并完成服务调用



### 消费方发起调用 - 代理

前面预留的UserService实现方法



肯定不能直接把impl实现类copy过来



通过架构图，可以通过生成代理对象来简化调用

#### 静态代理

为每一个特定类型的接口或对象，编写一个代理类



在`rpc-consumer`模块中，创建静态代理`UserServiceProxy`，实现UserService接口和getUser方法



实现getUser需要构造http请求去调用服务提供者

```java
package site.xiaofei.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.serializer.JdkSerializer;

import java.io.IOException;

/**
 * @author tuaofei
 * @description 静态代理
 * @date 2024/10/18
 */
public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        //指定序列化器
        JdkSerializer serializer = new JdkSerializer();

        //给rpc框架发送请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .paramTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();

        try {
            byte[] bodyBytes = serializer.serializer(rpcRequest);
            byte[] resultBytes;
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute();
            resultBytes = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserializer(resultBytes, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

然后修改RpcConsumerExample，new一个代理对象赋值给UserService

```java
public class RpcConsumerExample {

    public static void main(String[] args) {
        //静态代理
        UserService userService = new UserServiceProxy();
		...
    }
}
```

> 缺点：需要给每个服务接口都写一个实现类，太麻烦，不灵活

#### 动态代理

根据生成的对象的类型，自动生成一个代理对象



常用的代理实现方式JDK动态代理【只能对接口进行代理】、基于字节码生成的动态代理（CGLIB）【可对任何类进行代理，性能略低于JDK动态代理】



（1）在rpc-system模块中，编写动态代理类,需要实现InvocationHandler接口的invoke方法

```java
package site.xiaofei.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.serializer.JdkSerializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author tuaofei
 * @description 服务代理（jdk动态代理）
 * @date 2024/10/18
 */
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //指定序列化器
        JdkSerializer serializer = new JdkSerializer();

        //给rpc框架发送请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .args(args)
                .build();

        try {
            byte[] bodyBytes = serializer.serializer(rpcRequest);
            byte[] resultBytes;
            //todo 地址需要使用注册中心和服务发现机制解决
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute();
            resultBytes = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserializer(resultBytes, RpcResponse.class);
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

当用户调用某个接口方法时，会改为第调用invoke方法。在invoke中可以获取到调用的方法信息、传入的参数列表



（2）创建动态代理工厂ServiceProxyFactory，使用工厂模式，作用：根据指定类创建动态代理对象

目录结构

![image-20241019012807097](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019012807097.png)

```java
package site.xiaofei.proxy;

import java.lang.reflect.Proxy;

/**
 * @author tuaofei
 * @description 服务代理工厂（用于创建代理对象）
 * @date 2024/10/18
 */
public class ServiceProxyFactory {

    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }
}
```

（3）在RpcConsumerExample中，通过调用工厂来为UserService获取动态代理对象

```java
UserService userService = ServiceProxyFactory.getProxy(UserService.class);
```



### 测试

（1）debug启动服务提供者rpc-provider

![image-20241019013157960](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019013157960.png)

（2）debug启动消费者

在ServiceProxy代理类中断点，可以看到调用UserService，实际调用的是invoke方法，并且获取到参数信息

![image-20241019014335381](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019014335381.png)

（3）可以看到序列化后的请求对象，结构是字节数组

![image-20241019014345615](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019014345615.png)

（4）在服务提供者模块的请求处理器中断电，可以看到反序列化后的请求，和发送内容保持一致；请求完成通过反射调用方法，并得到了user对象

![image-20241019014524017](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019014524017.png)

（5）成功打印结果

![image-20241019015103397](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241019015103397.png)

但是出现了一个警告 has been blocked for 99196 ms, time limit is 60000 ms

貌似处理时长过长

可以把时间调长一点，这个不影响程序的处理；

在`VertxServer`创建Vertx实例时，传入配置参数

```java
VertxOptions vertxOptions = new VertxOptions();
        //设置最大事件循环执行时间的值；最大事件循环执行时间的默认值 = 2000000000 ns（2 秒）
        //Vert.x Api 是非阻塞，并且不会堵塞事件循环。
        //如果程序阻塞，或者执行了一段长时间的代码或者debug没放掉，检测到一段时间后事件循环还没有恢复，Vert.x会自动记录警告。如果你在日志中看到这样的警告
        //has been blocked for 2862 ms, time limit is 2000 ms
        //不建议执行的阻塞代码
        //1.Thread.sleep()
        //2.等待锁
        //3.等待互斥体或监视器 (例如同步段)
        //4.做一个长时间的数据库操作和等待返回
        //5.做复杂的计算，需要很长的时间。
        //6.死循环。
        vertxOptions.setMaxEventLoopExecuteTime(60).setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);

        //创建vertx实例
        Vertx vertx = Vertx.vertx(vertxOptions);
```

参考：

+ [黄金法则 — 不要阻塞事件循环 - 《Java API 版本的Vert.x Core 手册》 - 书栈网 · BookStack](https://www.bookstack.cn/read/vert-x-core-manual-for-java/黄金法则_不要阻塞事件循环.md)

+ [VertxOptions (Vert.x Stack - Docs 4.5.10 API)](https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html)

+ [Vert.x(六): Vert.x配置项VertxOptions的使用-CSDN博客](https://blog.csdn.net/mawei7510/article/details/83059684)



## 全局配置加载

在RPC框架运行过程中，有一些配置信息，比如注册中心的地址、序列化方式、网络服务器端口...，之前的项目中都是写死的，不利于维护。



通过配置文件来进行**自定义配置**



### 设计方案

#### 配置项

先提供一个简单的配置，后续再扩展

```
服务名称 name
版本 version
服务器主机名 serverHost
服务器端口号 serverPort
```



了解常见的RPC框架配置

1. 注册中心地址
2. 服务接口
3. 序列化方式
4. 网络通信协议
5. 超时设置
6. 负载均衡策略
7. 服务端线程模型



参考：[API 配置 | Apache Dubbo](https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/config/api/api/)





#### 读取配置文件

可以使用java的properties，这里使用第三方工具hutool的setting模块

参考：[设置文件-Setting | Hutool](https://doc.hutool.cn/pages/setting/example/#代码)



一般情况，读取配置文件名称application。properties，还可以指定文件名称后缀来区分多环境，比如：application-prod.properties表示生产环境、application-test.properties表示测试环境



### 开发实现

#### 项目初始化

（1）新增`rpc-core`模块，复用`rpc-system`代码

（2）引入xml依赖，日志和单元测试

`xiaofei.site-rpc`父级xml

```xml
<properties>
        <logback-version>1.3.12</logback-version>
        <junit-version>RELEASE</junit-version>
    </properties>

    <dependencyManagement>
            <dependency>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-classic</artifactId>
                <version>${logback-version}</version>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>${junit-version}</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

`rpc-core`模块xml

```xml
<dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </dependency>
```

（3）将`rpc-consumer`和`rpc-provider`项目引入的rpc依赖`rpc-system`都替换为`rpc-core`

```xml
<dependency>
            <groupId>xiaofei.site</groupId>
            <artifactId>rpc-core</artifactId>
            <version>1.0.0</version>
        </dependency>
```

#### 配置加载

（1）在config包下新建配置类`RpcConfig`

```java
package site.xiaofei.config;

import lombok.Data;

/**
 * @author tuaofei
 * @description rpc框架配置
 * @date 2024/10/20
 */
@Data
public class RpcConfig {

    /**
     * 服务名称
     */
    private String name = "xaiofei.site-rpc";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
}
```

(2)在utils包下新建工具类`ConfigUtils`，作用是读取配置文件并返回配置对象



工具类尽量通用不和业务强绑定。比如支持要读取配置内容前缀，文件名后缀，传入环境...

```java
package site.xiaofei.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * @author tuaofei
 * @description 配置工具类
 * @date 2024/10/20
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     *
     * @param tClass
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix,"", "");
    }

    /**
     * 加载配置对象，支持区分环境
     *
     * @param tClass
     * @param prefix
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String fileSuffix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        if (StrUtil.isNotBlank(fileSuffix)){
            configFileBuilder.append(fileSuffix);
        }else {
            configFileBuilder.append(".properties");
        }
        Props props = new Props(configFileBuilder.toString());
        return props.toBean(tClass, prefix);
    }
}
```

（3）在constant包中新建`RpcConstant`接口，用于存储rpc框架相关常量

```java
package site.xiaofei.constant;

/**
 * @author tuaofei
 * @description Rpc相关常量
 * @date 2024/10/20
 */
public interface RpcConstant {

    /**
     * 默认配置文件加载前缀
     */
    String DEFAULT_CONFIG_PREFIX = "rpc";
}
```

可以读取到类似下面的配置

```
rpc.name=rpc-consumer
rpc.version=1.0
rpc.serverPort=8081
```

#### 维护全局配置对象

在项目启动时，从配置文件读取配置并创建实例对象，之后就可以集中从这个配置对象获取信息，不用每次重新读取、并创建对象，减少性能开销



使用单例模式



一般会使用holder来维护全局配置对象实例，这里使用`RpcApplication`

```java
package site.xiaofei;

import lombok.extern.slf4j.Slf4j;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.constant.RpcConstant;
import site.xiaofei.utils.ConfigUtils;

/**
 * @author tuaofei
 * @description Rpc框架应用
 * 相当于holder，存放全局变量。双检锁单例模式
 * @date 2024/10/20
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，支持传入自定义配置
     *
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init,config = {}", newRpcConfig.toString());
    }

    /**
     * 初始化
     */
    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            //配置记载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * 双重检查锁单例模式：单例模式最佳实践
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }

}
```

上述代理就是双重检查锁单例模式的实现，支持在获取配置时才调用init方法实现懒加载。支持自己传入配置对象；如果没有，使用默认的加载配置

```java
RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
```

### 测试

#### 测试配置文件读取

在`rpc-consumer`的resources目录创建配置文件`application.properties`

```properties
rpc.name=rpc-consumer
rpc.version=1.0
rpc.serverPort=8081
```

![image-20241020212214688](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241020212214688.png)

创建`RpcConsumerEasyExample`，测试文件读取

```java
package site.xiaofei.consumer;

import site.xiaofei.config.RpcConfig;
import site.xiaofei.utils.ConfigUtils;

/**
 * @author tuaofei
 * @description 简易服务消费者示例
 * @date 2024/10/20
 */
public class RpcConsumerEasyExample {

    public static void main(String[] args) {
        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpcConfig);
        
    }
}
```

#### 测试全局配置对象加载

在`rpc-provider`创建`RpcProviderEasyExample`，能够根据配置动态在不同端口启动web服务

```java'
package site.xiaofei.provider;

import site.xiaofei.RpcApplication;
import site.xiaofei.common.service.UserService;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.server.HttpServer;
import site.xiaofei.server.VertxHttpServer;

/**
 * @author tuaofei
 * @description 简易服务提供者示例
 * @date 2024/10/20
 */
public class RpcProviderEasyExample {
    public static void main(String[] args) {

        //rpc框架初始化
        RpcApplication.init();

        //注册服务
        LocalRegistry.register(UserService.class.getName(),UserServiceImpl.class);

        //启动http服务
        HttpServer vertxServer = new VertxHttpServer();
        vertxServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
```

### 扩展

（1）支持读取application.yml、application.yml等不同格式的配置文件



改造ConfigUtils的loadConfig方法，增加参数fileSuffix文件后缀

```java
public static <T> T loadConfig(Class<T> tClass, String fileSuffix, String prefix) {
        return loadConfig(tClass, prefix, fileSuffix, "");
    }

public static <T> T loadConfig(Class<T> tClass, String prefix, String fileSuffix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        if (StrUtil.isNotBlank(fileSuffix)) {
            configFileBuilder.append(fileSuffix);
        } else {
            configFileBuilder.append(RpcConstant.DEFAULT_CONFIG_FILESUFFIX);
        }
        Props props = new Props(configFileBuilder.toString());
        return props.toBean(tClass, prefix);
    }
```

RpcConstant常量增加

```java
String DEFAULT_CONFIG_FILESUFFIX = ".properties";
String CONFIG_YML_FILESUFFIX = ".yml";
String CONFIG_YAML_FILESUFFIX = ".yaml";
```

（2）支持监听配置文件的变更，并自动更新配置对象（可使用props.autoLoad()）

（3）配置文件支持中文（注意编码问题）

（4）配置分组，后续配置增多，可以考虑对配置进行分组



## 接口mock

模拟接口对象，用于测试开发使用



通过动态代理创建一个调用方法时返回固定值的对象



### 开发实现

（1）支持通过修改配置文件的方式开启mock，修改全局配置类`RpcConfig`

```java
@Data
public class RpcConfig {

   ...

    /**
     * 模拟调用
     */
    private boolean mock = false;
}
```

(2)在proxy包下新增`MockServiceProxy`类，用于生成mock代理服务

```java
package site.xiaofei.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author tuaofei
 * @description Mock服务代理（jdk动态代理）
 * @date 2024/10/21
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> methodReturnType = method.getReturnType();
        log.info("mock invoke {}", method.getName());
        return getDefaultObject(methodReturnType);
    }

    private Object getDefaultObject(Class<?> returnType) {
        //基本类型
        if (returnType == boolean.class) {
            return false;
        } else if (returnType == short.class) {
            return (short) 0;
        } else if (returnType == int.class) {
            return 0;
        } else if (returnType == long.class) {
            return 0L;
        }
        return null;
    }
}
```

通过getDefaultObject方法，根据代理接口的class返回不同的默认值



（3）给`ServiceProxyFactory`服务代理工厂新增获取mock代理对象的方法`getMockProxy`。通过读取已定义的全局配置mock开区分创建哪种代理对象

```java
package site.xiaofei.proxy;

import site.xiaofei.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * @author tuaofei
 * @description 服务代理工厂（用于创建代理对象）
 * @date 2024/10/18
 */
public class ServiceProxyFactory {

    public static <T> T getProxy(Class<T> serviceClass) {
        if (RpcApplication.getRpcConfig().isMock()){
            return getMockProxy(serviceClass);
        }
        
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }

    /**
     * 根据服务获取mock代理对象
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getMockProxy(Class<T> serviceClass){
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy()
        );
    }
}
```

### 测试

（1）在`rpc-common`公共模块的UserService中新增一个默认实现的方法

```java
package site.xiaofei.common.service;

import site.xiaofei.common.model.User;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/17
 */
public interface UserService {

    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 获取数字
     * @return
     */
    default short getNumber(){
        return 1;
    }
}
```

（2）修改服务消费者的配置文件`application.properties`，将mock设置为true

```java
rpc.name=rpc-consumer
rpc.version=1.0
rpc.serverPort=8082
rpc.mock=true
```

(3)修改`RpcConsumerExample`类，编写调用`userService.getNumber`测试

```java
public class RpcConsumerExample {

    public static void main(String[] args) {
        //静态代理
//        UserService userService = new UserServiceProxy();
        //jdk动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("土澳菲");
        User resultUser = userService.getUser(user);
        if (resultUser != null){
            System.out.println(resultUser.getName());
        }else{
            System.out.println("user is null!");
        }
        short number = userService.getNumber();
        System.out.println(number);
    }
}
```

应该能看到输出结果为0，不是1，说明调用了MockServiceProxy模拟服务代理



> 注意rpc-core的ServiceProxy服务代理的HttpRequest.post地址需要修改为获取配置文件的地址

```java
//地址需要使用注册中心和服务发现机制解决
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            if (rpcConfig == null){
                throw new RuntimeException("get rpcConfig error");
            }
            String remoteUrl = String.format("http://%s:%s", rpcConfig.getServerHost(), rpcConfig.getServerPort());
            HttpResponse httpResponse = HttpRequest.post(remoteUrl)
                    .body(bodyBytes)
                    .execute();
```



### 扩展

完善mock的逻辑，支持更多返回类型的默认值生成（faker伪造数据生成库，生成默认值）



## 序列化器和SPI机制

通过前面使用的Jdk序列化器，对于一个完善的RPC框架，还需要考虑
1.是否有更好的序列化器实现方式？

2.怎么让使用框架的开发者指定使用的序列化器？

3.怎么让使用框架的开发者定制自己的序列化器？



### 设计方案



主流序列化方式对比

| 序列化方式            | 介绍                                     | 优点                                                         | 缺点                                                         |
| --------------------- | ---------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| JSON（此教程实现）    |                                          | 可读性强，便于理解和调试<br />跨语言支持广泛，几乎所有编程语言都用JSON的解析和生成库 | 序列化后的数据量相对较大，因为JSON使用文本格式存储数据，需要额外的字符表示键值和数据结构<br />不能很好地处理复杂的数结构和循环引用，可能导致性能下降或者序列化失败 |
| Hessian（此教程实现） | https://hessian.caucho.com/              | 二进制序列化，序列后的数据量较小，网络传输效率高<br />支持跨语言，适用于分布式系统中的服务调用 | 性能较`JSON`略低，因为需要将对象转换为二进制格式<br />对象必须实现`Serializable`接口，限制了可序列化的对象范围 |
| Kryo（此教程实现）    | https://github.com/EsotericSoftware/kryo | 高性能，序列化和反序列化速度快<br />支持循环引用和自定义序列化器，适用于复杂的对象结构<br />无需实现`Serializable`接口，可序列化任意对象 | 不夸语言，只适用于`java`<br />对象的序列化格式不够友好，不易懂，不便于调试 |
| Protobuf              |                                          | 高效的二进制序列化，序列化后的数据量极小<br />支持跨语言，并且提供多语言的实现库<br />支持版本化和向前/向后兼容性 | 配置相对复杂，需要先定义数据结构的消息格式<br />对象的序列化格式不易懂，不便于调试 |

### 动态使用序列化器

之前使用的硬编码

```java
//指定序列化器
JdkSerializer serializer = new JdkSerializer();
```

可以通过配置文件来指定使用的序列化器。在使用序列化器时，根据配置来获取不同的序列化器实例



参考Dubbo替换序列化协议：https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/serialization/hessian/



可以定义一个MAP<序列化器名称,序列化器实现类对象>，根据配置文件获取名称来查询对于的实例

### 自定义序列化器

如果不想使用框架的序列化器，想自己定义，怎么办？



RPC框架读取用户自定义的类路径，加载这个类，作为Serializer序列化器接口的实现



引入java中的重要特性：**SPI机制**



**什么是SPI？**

service provider interface 服务提供接口，用于实现模块化开发和插件化扩展



SPI机制允许服务提供者通过特定的配置文件将自己的实现注册到系统中，通过反射机制动态加载这些实现，不需要修改原始框架代码，实现系统的解耦，提高了可扩展性

例如：JDBC连接数据库，不同的数据库驱动开发者可以使用JDBS库，定制自己的数据库驱动

主流的开发框架，几乎都使用了，servlet容器、日志框架、ORM框架、Spring框架



**如何实现SPI？**



#### 系统实现

java内部提供了SPI机制相关的API接口，可以直接使用

（1）在`resources`资源目录下创建`META-INF/services`目录，并创建一个名称为要实现的接口的空文件

![image-20241022215151394](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241022215151394.png)

（2）在文件中填写定制接口实现类的**完整类路径**

![image-20241022215306513](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241022215306513.png)

（3）使用系统内置的`ServiceLoader`动态加载指定接口的实现类

```java
//指定序列化器
        Serializer serializer = null;
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer service : serviceLoader) {
            serializer = service;
        }
```



#### 自定义SPI实现

如果想定制多个不同的接口实现类，就不能指定使用哪一个了



需要定义SPI机制的实现，只要能够根据配置加载到类



读取配置文件，得到`序列化器名称 -> 序列化器实现类对象` 映射,根据配置的序列化器名称加载指定实现类对象

```
jdk=site.xiaofei.serializer.JdkSerializer
hessian=site.xiaofei.serializer.HessianSerializer
kryo=site.xiaofei.serializer.KryoSerializer
json=site.xiaofei.serializer.JsonSerializer
```

### 开发实现

#### 多种序列化器实现

(1)给`rpc-core`模块，pom.xml引入依赖

```xml
<!--        序列化-->
        <dependency>
            <groupId>com.caucho</groupId>
            <artifactId>hessian</artifactId>
            <version>4.0.66</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.esotericsoftware/kryo -->
        <dependency>
            <groupId>com.esotericsoftware</groupId>
            <artifactId>kryo</artifactId>
            <version>5.6.0</version>
        </dependency>
<!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.18.0</version>
        </dependency>
```

(2)在 `serializer`下实现各自的序列化器



##### JSON序列化器

JSON序列化器实现相对复杂，需要考虑对象转换兼容问题，比如object数组在序列化后会丢失类型

```java
package site.xiaofei.serializer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;

import java.io.IOException;

/**
 * @author tuaofei
 * @description json序列化器
 * @date 2024/10/22
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serializer(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> classType) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, classType);
        if (obj instanceof RpcRequest) {
            return handleRequest((RpcRequest) obj, classType);
        }
        if (obj instanceof RpcResponse) {
            return handleResponse((RpcResponse) obj, classType);
        }
        return OBJECT_MAPPER.readValue(bytes, classType);
    }

    /**
     * 由于Object的原始对象会被擦除，导致反序列化时会被作为linkedhashmap无法转换为原始对象，所以在这做特殊处理
     *
     * @param rpcRequest
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] paramTypes = rpcRequest.getParamTypes();
        Object[] args = rpcRequest.getArgs();

        //循环处理每个参数的类型
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> clazz = paramTypes[i];
            //如果类型不同，重新处理一下
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 由于Object的原始对象会被擦除，导致反序列化时会被作为linkedhashmap无法转换为原始对象，所以在这做特殊处理
     *
     * @param rpcResponse
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        //处理响应数据
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(bytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }
}
```

##### kryo序列化器

kryo本身是线程不安全的，所以使用ThreadLocal保证每个线程有一个单独的Kryo对象实例

```java
package site.xiaofei.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author tuaofei
 * @description Kryo序列化器
 * @date 2024/10/23
 */
public class KryoSerializer implements Serializer {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        //设置动态序列化和反序列化类，不提前注册所有类（可能存在安全问题）
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public <T> byte[] serializer(T object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        KRYO_THREAD_LOCAL.get().writeObject(output, object);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        T result = KRYO_THREAD_LOCAL.get().readObject(input, type);
        input.close();
        return result;
    }
}
```



##### Hessian序列化器

实现比较简单

```java
package site.xiaofei.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author tuaofei
 * @description Hessian序列化器
 * @date 2024/10/23
 */
public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serializer(T object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HessianOutput hessianOutput = new HessianOutput(outputStream);
        hessianOutput.writeObject(object);
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        HessianInput hessianInput = new HessianInput(byteArrayInputStream);
        return (T) hessianInput.readObject(type);
    }
}
```



#### 动态使用序列化器

序列化器所有代码均在`Serializer`包下



（1）定义序列化器名称的常量，使用接口实现

```java
package site.xiaofei.serializer;

/**
 * @author tuaofei
 * @description 序列化器 key
 * @date 2024/10/23
 */
public interface SerializerKeys {

    String JDK = "jdk";
    String JSON = "json";
    String KRYO = "Kryo";
    String HESSIAN = "Hessian";
}
```

(2)序列化器工厂

序列化器对象可复用，没必要每次都创建；使用工厂+单例模式来创建和获取序列化对象

```java
package site.xiaofei.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuaofei
 * @description 序列化器工厂（用于获取序列化对象）
 * @date 2024/10/23
 */
public class SerializerFactory {

    /**
     * 序列化映射（用于单例实现）
     */
    private static final Map<String,Serializer> KEY_SERIALIZER_MAP = new HashMap<String,Serializer>(){
        {
            put(SerializerKeys.JDK,new JdkSerializer());
            put(SerializerKeys.JSON,new JsonSerializer());
            put(SerializerKeys.KRYO,new KryoSerializer());
            put(SerializerKeys.HESSIAN,new HessianSerializer());
        }
    };

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get(SerializerKeys.JDK);

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getInstance(String key){
        return KEY_SERIALIZER_MAP.getOrDefault(key,DEFAULT_SERIALIZER);
    }

}
```

（3）在全局配置类RpcConfig中补充序列化器的配置

```java
@Data
public class RpcConfig {

  ...

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;
}
```

(4)动态获取序列化器

修改以下类获取序列化器的方式

```
ServiceProxy
HttpServerHandler
```

```java
//指定序列化器
final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
```

#### 自定义序列化器

支持用户自定义序列化器并指定key



（1）指定SPI配置目录

系统内置的SPI会加载`resources`资源目录下的`META-INF/services`目录，那我们自定义可改为`META-INF/rpc`目录



还可将系统内置SPI和用户自定义SPI

+ 用户自定义SPI：META-INF/rpc/custom；用户可在该目录下新增配置，加载自定义的实现类
+ 系统内置SPI：META-INF/rpc/system；RPC框架自带的实现类

这样所有的接口实现类都通过SPI动态加载，就不用在代码中写死了



创建`site.xiaofei.serializer.Serializer`

```
jdk=site.xiaofei.serializer.JdkSerializer
json=site.xiaofei.serializer.JsonSerializer
kryo=site.xiaofei.serializer.KryoSerializer
hessian=site.xiaofei.serializer.HessianSerializer
```

(2)编写SpiLoader加载器

读取配置并加载实现类



关键实现：

> 1.使用Map存储已加载的配置信息 `key -> 实现类`
>
> 2.扫描指定路径，读取每个配置文件，获取到`key -> 实现类`信息存储在map中
>
> 3.定义获取实例方法，根据用户传入的接口和key，从map中找到对应的是实现类，通过反射获取实现类对象
>
> 可以维护一个对象实例缓存，创建过的对象从缓存读取

```java
package site.xiaofei.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import site.xiaofei.serializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tuaofei
 * @description SPI加载器（支持键值对映射）
 * @date 2024/10/23
 */
@Slf4j
public class SpiLoader {

    /**
     * 存储已加载的类：接口名-><key,实现类>
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存（避免重复new），类路径->对象实例，单例模式
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义SPI目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("load all SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 加载某个类型
     *
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("load type is {} SPI", loadClass.getName());
        //扫描路径，用户自定义的SPI优先级高于系统SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            //读取每个资源文件
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArr = line.split("=");
                        if (strArr.length > 1) {
                            String key = strArr[0];
                            String className = strArr[1];
                            keyClassMap.put(key, Class.forName(className));
                        }

                    }
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> ketClassMap = loaderMap.get(tClassName);
        if (ketClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader not %s type", tClassName));
        }
        if (!ketClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader %s is exist key= %s type", tClassName, key));
        }
        //获取要加载的实现类型
        Class<?> implClass = ketClassMap.get(key);
        //从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s class instance fail", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }
}
```

上述代码，虽然提供了loadAll方法，扫描所有路径下的文件进行加载，但其实没必要使用。更推荐load方法，按需加载指定的类

> 注意：上述代码中获取配置文件使用了`ResourceUtil.getResources`，而不是通过文件路径获取，如果框架作为依赖被引入，是无法得到正确的文件路径的

（3）重构序列化器工厂

```java
package site.xiaofei.serializer;

import site.xiaofei.utils.SpiLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuaofei
 * @description 序列化器工厂（用于获取序列化对象）
 * @date 2024/10/23
 */
public class SerializerFactory {

    /**
     * 序列化映射（用于单例实现）
     */
    /*private static final Map<String,Serializer> KEY_SERIALIZER_MAP = new HashMap<String,Serializer>(){
        {
            put(SerializerKeys.JDK,new JdkSerializer());
            put(SerializerKeys.JSON,new JsonSerializer());
            put(SerializerKeys.KRYO,new KryoSerializer());
            put(SerializerKeys.HESSIAN,new HessianSerializer());
        }
    };*/

    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
```

使用静态代码块，在工厂首次加载时，调用SpiLoader.load加载序列化接口的实现类，就可以通过getInstance获取指定的实现类对象

### 测试

#### SPI加载测试

测试custom和system下的SPI配置文件是否成功加载

![image-20241024204041687](https://note-1259190304.cos.ap-chengdu.myqcloud.com/noteimage-20241024204041687.png)

测试正常key和异常的情况，比如不存在key

测试key相同时，自定义配置能否覆盖系统配置



#### 完整测试

（1）修改消费者和生产者配置文件，指定不同的序列化器

```properties
rpc.name=xiaofei.site-rpc
rpc.version=1.0
rpc.serverPort=8082
rpc.mock=false
rpc.serializer=hessian
```

(2)启动生产者和消费者，验证能否正常完成请求



#### 自定义序列化器

1.写一个类实现Serializer接口

2.在custom目录下编写spi配置文件，加载该类



### 扩展

（1）实现更多不同协议的序列化器

（2）序列化器工厂可使用懒加载创建序列化器实例

（3）SPI loader支持懒加载，获取实例时才加载对应的类



## 注册中心基本实现



