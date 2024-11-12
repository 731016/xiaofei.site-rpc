package site.xiaofei.fault.retry;

import site.xiaofei.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @author tuaofei
 * @description 重试策略
 * @date 2024/11/12
 */
public interface RetryStrategy {

    /**
     * 重试
     * @param callable
     * @return
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
