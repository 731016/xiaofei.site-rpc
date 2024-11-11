package site.xiaofei.loadbalancer;

import site.xiaofei.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author tuaofei
 * @description 负载均衡器（消费端使用）
 * @date 2024/11/11
 */
public interface LoadBalancer {

    /**
     * 选择服务调用
     * @param requestParams
     * @param serviceMetaInfoList
     * @return
     */
    ServiceMetaInfo select(Map<String,Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
