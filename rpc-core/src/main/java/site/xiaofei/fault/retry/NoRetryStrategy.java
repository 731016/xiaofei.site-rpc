package site.xiaofei.fault.retry;

import site.xiaofei.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @author tuaofei
 * @description 不重试策略
 * @date 2024/11/12
 */
public class NoRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
