package site.xiaofei.serializer;

import site.xiaofei.utils.SpiLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuaofei
 * @description 序列化器工厂（用于获取序列化对象）
 * @date 2024/10/23
 */
public class SerializerFactory {

    /**
     * 序列化映射（用于单例实现）
     */
    /*private static final Map<String,Serializer> KEY_SERIALIZER_MAP = new HashMap<String,Serializer>(){
        {
            put(SerializerKeys.JDK,new JdkSerializer());
            put(SerializerKeys.JSON,new JsonSerializer());
            put(SerializerKeys.KRYO,new KryoSerializer());
            put(SerializerKeys.HESSIAN,new HessianSerializer());
        }
    };*/

    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
