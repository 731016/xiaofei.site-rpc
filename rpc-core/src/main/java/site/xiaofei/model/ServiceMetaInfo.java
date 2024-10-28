package site.xiaofei.model;

import site.xiaofei.constant.RpcConstant;

/**
 * @author tuaofei
 * @description 服务元信息（注册信息）
 * @date 2024/10/28
 */
public class ServiceMetaInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePost;

    /**
     * 服务分组（暂未实现）
     */
    private String serviceGroup = "default";

    /**
     * 获取服务键名
     *
     * @return
     */
    public String getServiceKey() {
        //后续可扩展服务分组
//        return String.format("%s:%s",serviceName,serviceVersion,serviceGroup);
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务注册节点键名
     *
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePost);
    }
}
