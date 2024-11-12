package site.xiaofei.fault.retry;

/**
 * @author tuaofei
 * @description 重试策略key
 * @date 2024/11/12
 */
public interface RetryStrategyKeys {
    /**
     * 不重试
     */
    String NO = "no";

    /**
     * 固定时间间隔
     */
    String FIXED_INTERVAL = "fixedInterval";
}
