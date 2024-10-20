package site.xiaofei;

import lombok.extern.slf4j.Slf4j;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.constant.RpcConstant;
import site.xiaofei.utils.ConfigUtils;

/**
 * @author tuaofei
 * @description Rpc框架应用
 * 相当于holder，存放全局变量。双检锁单例模式
 * @date 2024/10/20
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，支持传入自定义配置
     *
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init,config = {}", newRpcConfig.toString());
    }

    /**
     * 初始化
     */
    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_FILESUFFIX, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            //配置记载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * 双重检查锁单例模式：单例模式最佳实践
     *
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }

}
