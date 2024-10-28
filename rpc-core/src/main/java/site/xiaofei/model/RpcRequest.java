package site.xiaofei.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.xiaofei.constant.RpcConstant;

import java.io.Serializable;

/**
 * @author tuaofei
 * @description Rpc请求
 * @date 2024/10/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 服务版本
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 参数类型列表
     */
    private Class<?>[] paramTypes;

    /**
     * 参数列表
     */
    private Object[] args;

}
