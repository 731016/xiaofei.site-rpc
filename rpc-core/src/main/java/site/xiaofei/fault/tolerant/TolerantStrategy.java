package site.xiaofei.fault.tolerant;

import site.xiaofei.model.RpcResponse;

import java.util.Map;

/**
 * @author tuaofei
 * @description 容错策略
 * @date 2024/11/13
 */
public interface TolerantStrategy {

    /**
     * 容错
     * @param context
     * @param e
     * @return
     */
    RpcResponse doTolerant(Map<String,Object> context,Exception e);
}
