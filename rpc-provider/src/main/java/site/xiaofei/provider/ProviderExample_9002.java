package site.xiaofei.provider;

import site.xiaofei.RpcApplication;
import site.xiaofei.common.service.UserService;
import site.xiaofei.config.RegistryConfig;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.model.ServiceMetaInfo;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.registry.Registry;
import site.xiaofei.registry.RegistryFactory;
import site.xiaofei.registry.RegistryKeys;
import site.xiaofei.server.tcp.VertxTcpServer;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/11/11
 */
public class ProviderExample_9002 {

    public static void main(String[] args) {
        //rpc框架初始化
        RpcConfig rpcConfig_9002 = new RpcConfig();
        rpcConfig_9002.setServerPort(9002);
        RegistryConfig registryConfig_9002 = rpcConfig_9002.getRegistryConfig();
        registryConfig_9002.setRegistry(RegistryKeys.ZOOKEEPER);
        registryConfig_9002.setAddress(RegistryKeys.ZOOKEEPER_REGISTER_SERVER_ADDRESS);
        RpcApplication.init(rpcConfig_9002);

        //注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        //注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePost(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //启动web服务
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());

        //启动tcp服务
        VertxTcpServer tcpServer = new VertxTcpServer();
        tcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
