package site.xiaofei.fault.retry;

import org.junit.Test;
import site.xiaofei.model.RpcResponse;

/**
 * @author tuaofei
 * @description 重试策略测试
 * @date 2024/11/12
 */
public class RetryStrategyTest {

    RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();

    @Test
    public void doRetry(){
        RpcResponse rpcResponse = null;
        try {
            rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}
