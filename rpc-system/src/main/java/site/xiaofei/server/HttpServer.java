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
