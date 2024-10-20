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
