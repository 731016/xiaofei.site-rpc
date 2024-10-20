package site.xiaofei.config;

import lombok.Data;

/**
 * @author tuaofei
 * @description rpc框架配置
 * @date 2024/10/20
 */
@Data
public class RpcConfig {

    /**
     * 服务名称
     */
    private String name = "xaiofei.site-rpc";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
}
