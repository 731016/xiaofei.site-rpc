package site.xiaofei.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import site.xiaofei.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author tuaofei
 * @description 一致性hash负载均衡器
 * @date 2024/11/11
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性hash环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodeMap = new TreeMap<>();

    /**
     * 虚拟节点数
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            return null;
        }
        //构建虚拟节点环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodeMap.put(hash, serviceMetaInfo);
            }
        }

        //获取调用请求的hash值
        int hash = getHash(requestParams);

        //选择最接近其>=调用请求hash值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodeMap.ceilingEntry(hash);
        if (entry == null){
            //如果没有，则返回环首部的节点
            entry = virtualNodeMap.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * hash算法，可自行实现
     *
     * @param key
     * @return
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
