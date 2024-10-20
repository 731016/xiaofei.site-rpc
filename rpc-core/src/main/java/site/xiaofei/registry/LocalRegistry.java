package site.xiaofei.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tuaofei
 * @description 本地注册中心
 * @date 2024/10/17
 */
public class LocalRegistry {

    /**
     * 注册信息存储
     */
    private static final Map<String,Class<?>> localRegistryInfoMap = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName,Class<?> implClass){
        localRegistryInfoMap.put(serviceName, implClass);
    }

    /**
     * 获取服务
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName){
        return localRegistryInfoMap.get(serviceName);
    }

    public static void remove(String serviceName){
        localRegistryInfoMap.remove(serviceName);
    }

}
