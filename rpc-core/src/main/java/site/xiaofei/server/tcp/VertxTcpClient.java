package site.xiaofei.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/11/4
 */
public class VertxTcpClient {

    public void start() {
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("connect to tcp server");
                NetSocket socket = result.result();
                for (int i = 0; i < 1000; i++) {
                    //发送数据
                    socket.write("hello,server!hello,server!hello,server!hello,server!");
                }
                //接收响应
                socket.handler(buffer -> {
                    System.out.println("received response from server :" + buffer.toString());
                });
            } else {
                System.out.println("failed to connect tcp server");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
