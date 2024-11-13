package site.xiaofei.fault.tolerant;

import site.xiaofei.model.RpcResponse;

import java.util.Map;

/**
 * @author tuaofei
 * @description 故障转移容错
 * @date 2024/11/13
 */
public class FailOverTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //todo 获取其它节点并调用
        return null;
    }
}
