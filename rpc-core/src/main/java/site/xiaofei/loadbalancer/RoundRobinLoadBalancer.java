package site.xiaofei.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import site.xiaofei.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tuaofei
 * @description 轮询负载均衡器
 * @date 2024/11/11
 */
public class RoundRobinLoadBalancer implements LoadBalancer{

    /**
     * 当前轮询的下标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollUtil.isEmpty(serviceMetaInfoList)){
            return null;
        }
        int size = serviceMetaInfoList.size();
        if (size == 1){
            return serviceMetaInfoList.get(0);
        }
        //取模算法轮询
        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
