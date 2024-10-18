package site.xiaofei.proxy;

import java.lang.reflect.Proxy;

/**
 * @author tuaofei
 * @description 服务代理工厂（用于创建代理对象）
 * @date 2024/10/18
 */
public class ServiceProxyFactory {

    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }
}
