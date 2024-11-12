package site.xiaofei.fault.retry;

import site.xiaofei.utils.SpiLoader;

/**
 * @author tuaofei
 * @description 重试策略工厂
 * @date 2024/11/12
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
