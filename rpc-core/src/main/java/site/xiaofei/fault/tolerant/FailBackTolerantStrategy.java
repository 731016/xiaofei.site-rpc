package site.xiaofei.fault.tolerant;

import lombok.extern.slf4j.Slf4j;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.proxy.ServiceProxyFactory;

import java.util.Map;

/**
 * @author tuaofei
 * @description 失败自动恢复容错
 * @date 2024/11/13
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //todo 服务降级并调用mock
        log.error("容错策略 failBack {}", context);
        return null;
    }
}
