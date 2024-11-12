package site.xiaofei.fault.retry;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import site.xiaofei.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author tuaofei
 * @description 固定时间充实策略
 * @date 2024/11/12
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        /**
         * 设置重试条件，如果抛出的异常是Exception类型或其子类型的实例，那么就会进行重试。
         * 设置等待策略，每次重试之间固定等待3秒钟。
         * 设置停止策略，最多重试3次后停止。
         * 设置重试监听器，每次重试时都会调用onRetry方法，这里在onRetry方法中记录了重试的次数。
         */
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .retryIfExceptionOfType(RuntimeException.class)
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                }).build();
        return retryer.call(callable);
    }
}
