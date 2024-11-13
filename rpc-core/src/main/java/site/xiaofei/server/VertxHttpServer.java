package site.xiaofei.server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

/**
 * @author tuaofei
 * @description 基于vertx实现的web服务器
 * @date 2024/10/17
 */
public class VertxHttpServer implements HttpServer {
    @Override
    public void doStart(int port) {
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

        //创建http服务器
        io.vertx.core.http.HttpServer vertxHttpServer = vertx.createHttpServer();

        //监听端口并处理请求
        vertxHttpServer.requestHandler(new HttpServerHandler());

        //启动http服务器并监听指定端口
        vertxHttpServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println(String.format("http server is now listening on port %s", port));
            } else {
                System.out.println(String.format("failed to start server %s", result.cause()));
            }
        });
    }
}
