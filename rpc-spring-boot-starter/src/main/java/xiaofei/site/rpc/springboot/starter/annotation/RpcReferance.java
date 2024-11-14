package xiaofei.site.rpc.springboot.starter.annotation;


import org.springframework.stereotype.Component;
import site.xiaofei.constant.RpcConstant;
import site.xiaofei.fault.retry.RetryStrategyKeys;
import site.xiaofei.fault.tolerant.TolerantStrategyKeys;
import site.xiaofei.loadbalancer.LoadBalancerKeys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务消费者注解（用于注入服务）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReferance {

    /**
     * 服务接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     * @return
     */
    String loadBalancer() default LoadBalancerKeys.CONSISTENT_HASH;

    /**
     * 重试策略
     * @return
     */
    String retryStrategy() default RetryStrategyKeys.NO;

    /**
     * 容错策略
     * @return
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_OVER;

    /**
     * 模拟调用
     * @return
     */
    boolean mock() default false;
}
