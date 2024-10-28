package site.xiaofei.config;

import lombok.Data;

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
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2380";

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
