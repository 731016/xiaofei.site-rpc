package site.xiaofei.config;

import lombok.Data;
import site.xiaofei.registry.RegistryKeys;

/**
 * @author tuaofei
 * @description Rpc框架注册中心配置
 * @date 2024/10/28
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心类别
     */
    private String registry = RegistryKeys.ETCD;

    /**
     * 注册中心地址
     */
    private String address = RegistryKeys.ETCD_REGISTER_SERVER_ADDRESS;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间，毫秒
     */
    private Long timeout = 10000L;
}
