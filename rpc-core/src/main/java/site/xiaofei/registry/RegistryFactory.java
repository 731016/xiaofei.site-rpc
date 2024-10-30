package site.xiaofei.registry;

import site.xiaofei.utils.SpiLoader;

/**
 * @author tuaofei
 * @description 注册中心工厂（用于获取注册中心对象）
 * @date 2024/10/30
 */
public class RegistryFactory {

    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
