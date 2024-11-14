package xiaofei.site.rpc.springboot.starter.bootstrap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import site.xiaofei.RpcApplication;
import site.xiaofei.config.RegistryConfig;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.model.ServiceMetaInfo;
import site.xiaofei.registry.LocalRegistry;
import site.xiaofei.registry.Registry;
import site.xiaofei.registry.RegistryFactory;
import xiaofei.site.rpc.springboot.starter.annotation.RpcService;

/**
 * @author tuaofei
 * @description Rpc服务提供者启动
 * @date 2024/11/14
 */
public class RpcProviderBootstrap implements BeanPostProcessor {


    /**
     * bean初始化后执行，注册服务
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null){
            //需要注册服务信息

            //1.获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            //默认值处理
            if (void.class == interfaceClass){
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();

            //2.注册服务
            //本地注册
            LocalRegistry.register(serviceName, beanClass);

            //全局配置
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();

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

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
