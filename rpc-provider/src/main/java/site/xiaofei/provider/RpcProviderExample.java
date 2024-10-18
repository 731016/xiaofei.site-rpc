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
