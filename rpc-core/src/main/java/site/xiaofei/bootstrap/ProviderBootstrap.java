package site.xiaofei.bootstrap;

import site.xiaofei.RpcApplication;
import site.xiaofei.config.RegistryConfig;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.model.ServiceMetaInfo;
import site.xiaofei.model.ServiceRegisterInfo;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.registry.Registry;
import site.xiaofei.registry.RegistryFactory;
import site.xiaofei.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * @author tuaofei
 * @description 服务提供者启动类
 * @date 2024/11/14
 */
public class ProviderBootstrap {

    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        //rpc框架初始化
        RpcApplication.init();

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            Class<?> implClass = serviceRegisterInfo.getImplClass();
            LocalRegistry.register(serviceName, implClass);

            //注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePost(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        //启动web服务
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());

        //启动tcp服务
        VertxTcpServer tcpServer = new VertxTcpServer();
        tcpServer.doStart(rpcConfig.getServerPort());
    }
}
