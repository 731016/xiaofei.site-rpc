package site.xiaofei.serializer;

import java.io.IOException;

/**
 * @author xiaofei
 * @description 序列化接口
 * @date 2024/10/17
 */
public interface Serializer {

    /**
     * 序列化
     * @param object
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> byte[] serializer(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T deserializer(byte[] bytes,Class<T> type) throws IOException;
}
