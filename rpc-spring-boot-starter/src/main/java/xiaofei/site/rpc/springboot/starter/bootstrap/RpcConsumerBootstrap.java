package xiaofei.site.rpc.springboot.starter.bootstrap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import site.xiaofei.proxy.ServiceProxyFactory;
import xiaofei.site.rpc.springboot.starter.annotation.RpcReferance;

import java.lang.reflect.Field;

/**
 * @author tuaofei
 * @description Rpc服务消费者启动
 * @date 2024/11/14
 */
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * bean初始化后执行，注入服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        //遍历对象的所有属性
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            RpcReferance rpcReferance = field.getAnnotation(RpcReferance.class);
            if (rpcReferance != null) {
                //为属性生成代理对象
                Class<?> interfaceClass = rpcReferance.interfaceClass();
                if (void.class == interfaceClass) {
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败", e);
                }
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
