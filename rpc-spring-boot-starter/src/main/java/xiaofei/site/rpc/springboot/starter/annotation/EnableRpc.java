package xiaofei.site.rpc.springboot.starter.annotation;

import org.springframework.context.annotation.Import;
import xiaofei.site.rpc.springboot.starter.bootstrap.RpcConsumerBootstrap;
import xiaofei.site.rpc.springboot.starter.bootstrap.RpcInitBootstrap;
import xiaofei.site.rpc.springboot.starter.bootstrap.RpcProviderBootstrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动rpc注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动server
     *
     * @return
     */
    boolean needServer() default true;
}
