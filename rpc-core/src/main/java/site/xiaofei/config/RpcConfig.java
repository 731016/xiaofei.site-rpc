package site.xiaofei.config;

import lombok.Data;
import site.xiaofei.fault.retry.RetryStrategyKeys;
import site.xiaofei.loadbalancer.LoadBalancerKeys;
import site.xiaofei.registry.Registry;
import site.xiaofei.serializer.SerializerKeys;

/**
 * @author tuaofei
 * @description rpc框架配置
 * @date 2024/10/20
 */
@Data
public class RpcConfig {

    /**
     * 服务名称
     */
    private String name = "xaiofei.site-rpc";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;
}
