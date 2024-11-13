package site.xiaofei.fault.tolerant;

import site.xiaofei.utils.SpiLoader;

/**
 * @author tuaofei
 * @description 容错策略工厂
 * @date 2024/11/13
 */
public class TolerantStrategyFactory {

    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}
