package site.xiaofei.server.tcp;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import site.xiaofei.RpcApplication;
import site.xiaofei.server.HttpServer;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/11/4
 */
@Slf4j
public class VertxTcpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        //创建实例
        Vertx vertx = Vertx.vertx();

        //创建tcp服务器
        NetServer server = vertx.createNetServer();

        //处理请求
        server.connectHandler(new TcpServerHandler());
        /*server.connectHandler(socket -> {
            //处理连接
            socket.handler(buffer -> {
                RecordParser recordParser = RecordParser.newFixed(8);
                recordParser.setOutput(new Handler<Buffer>() {

                    //初始化
                    int size = -1;
                    //一次完整的读取（header + body）
                    Buffer resultBuffer = Buffer.buffer();

                    @Override
                    public void handle(Buffer buffer) {
                        if (-1 == size){
                            //读取消息体长度
                            size = buffer.getInt(4);
                            recordParser.fixedSizeMode(size);
                            //写入头信息
                            resultBuffer.appendBuffer(buffer);
                        }else{
                            //写入消息体信息
                            resultBuffer.appendBuffer(buffer);
                            log.info(resultBuffer.toString());
                            //重置一轮
                            recordParser.fixedSizeMode(8);
                            size = -1;
                            resultBuffer = Buffer.buffer();
                        }
                    }
                });
                socket.handler(recordParser);
            });
        });*/

        //启动tcp服务并监听
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("tcp server started on port" + port);
            } else {
                System.out.println("failed to start tcp server" + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(RpcApplication.getRpcConfig().getServerPort());
//        new VertxTcpServer().doStart(8888);
    }
}
