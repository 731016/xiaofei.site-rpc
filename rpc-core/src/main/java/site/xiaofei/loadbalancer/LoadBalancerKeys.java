package site.xiaofei.loadbalancer;

/**
 * @author tuaofei
 * @description 负载均衡常量
 * @date 2024/11/11
 */
public interface LoadBalancerKeys {

    String ROUND_ROBIN = "roundRobin";
    String CONSISTENT_HASH = "consistentHash";
    String RANDOM = "randomLoad";
}
