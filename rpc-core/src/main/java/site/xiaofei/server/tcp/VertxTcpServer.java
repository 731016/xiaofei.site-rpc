package site.xiaofei.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import site.xiaofei.RpcApplication;
import site.xiaofei.server.HttpServer;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/11/4
 */
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
        server.connectHandler(new TcpServerHandler());

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
    }
}
