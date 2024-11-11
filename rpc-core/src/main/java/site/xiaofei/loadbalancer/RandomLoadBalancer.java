package site.xiaofei.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import site.xiaofei.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author tuaofei
 * @description 随机负载均衡器
 * @date 2024/11/11
 */
public class RandomLoadBalancer implements LoadBalancer{

    private final Random random = new Random();

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollUtil.isEmpty(serviceMetaInfoList)){
            return null;
        }
        int size = serviceMetaInfoList.size();
        if (size == 1){
            return serviceMetaInfoList.get(0);
        }
        return serviceMetaInfoList.get(random.nextInt(size));
    }
}
