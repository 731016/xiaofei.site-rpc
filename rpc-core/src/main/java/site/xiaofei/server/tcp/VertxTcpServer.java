package site.xiaofei.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
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

    private byte[] handleRequest(byte[] requestData) {
        return "Hello , client".getBytes();
    }

    @Override
    public void doStart(int port) {
        //创建实例
        Vertx vertx = Vertx.vertx();

        //创建tcp服务器
        NetServer server = vertx.createNetServer();

        //处理请求
        /*server.connectHandler(socket -> {
            //处理连接
            socket.handler(buffer -> {
                //处理接收到的字节数组
                byte[] requestData = buffer.getBytes();
                byte[] responseData = handleRequest(requestData);
                //发送响应
                socket.write(Buffer.buffer(responseData));
            });
        });*/
//        server.connectHandler(new TcpServerHandler());
        server.connectHandler(socket -> {
            //处理连接
            socket.handler(buffer -> {
                String testMessage = "hello,server!hello,server!hello,server!hello,server!";
                int messageLength = testMessage.getBytes().length;
                if (buffer.getBytes().length < messageLength) {
                    log.warn("半包，length = " + buffer.getBytes().length);
                    return;
                }
                if (buffer.getBytes().length > messageLength) {
                    log.warn("粘包，length = " + buffer.getBytes().length);
                    return;
                }
                String str = new String(buffer.getBytes(0, messageLength));
                log.info(str);
                if (testMessage.equals(str)) {
                    log.info("good");
                }
                //处理接收到的字节数组
                byte[] requestData = buffer.getBytes();
                byte[] responseData = handleRequest(requestData);
                //发送响应
                socket.write(Buffer.buffer(responseData));
            });
        });

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
//        new VertxTcpServer().doStart(RpcApplication.getRpcConfig().getServerPort());
        new VertxTcpServer().doStart(8888);
    }
}
