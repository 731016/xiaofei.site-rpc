package site.xiaofei.consumer;

import site.xiaofei.config.RpcConfig;
import site.xiaofei.utils.ConfigUtils;

/**
 * @author tuaofei
 * @description 简易服务消费者示例
 * @date 2024/10/20
 */
public class RpcConsumerEasyExample {

    public static void main(String[] args) {
        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class,"", "rpc");
        System.out.println(rpcConfig);

    }
}
