package site.xiaofei.loadbalancer;

import site.xiaofei.utils.SpiLoader;

/**
 * @author tuaofei
 * @description 负载均衡器工厂
 * @date 2024/11/11
 */
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    private static final LoadBalancer DEFAULT_LOADBALANCER = new RoundRobinLoadBalancer();

    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }

}
