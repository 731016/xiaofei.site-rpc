package site.xiaofei.fault.tolerant;

import cn.hutool.core.collection.CollUtil;
import site.xiaofei.RpcApplication;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.fault.retry.RetryStrategy;
import site.xiaofei.fault.retry.RetryStrategyFactory;
import site.xiaofei.loadbalancer.LoadBalancer;
import site.xiaofei.loadbalancer.LoadBalancerFactory;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.model.ServiceMetaInfo;
import site.xiaofei.server.tcp.VertxTcpClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author tuaofei
 * @description 故障转移容错
 * @date 2024/11/13
 */
public class FailOverTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //获取其它节点并调用
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceMetaInfoList");
        ServiceMetaInfo selectedServiceMetaInfo = (ServiceMetaInfo) context.get("selectedServiceMetaInfo");

        //移除失败节点
        removeFailNode(selectedServiceMetaInfo,serviceMetaInfoList);

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("methodName", rpcRequest.getMethodName());

        RpcResponse rpcResponse = null;
        while (serviceMetaInfoList.size() > 0 || rpcResponse != null) {
            ServiceMetaInfo currentServiceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
            System.out.println("获取节点：" + currentServiceMetaInfo);
            try {
                //发送tcp请求
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() -> VertxTcpClient.doRequest(rpcRequest, currentServiceMetaInfo));
                return rpcResponse;
            } catch (Exception exception) {
                //移除失败节点
                removeFailNode(currentServiceMetaInfo,serviceMetaInfoList);
                continue;
            }
        }
        //调用失败
        throw new RuntimeException(e);
    }

    /**
     * 移除失败节点，可考虑下线
     *
     * @param serviceMetaInfoList
     */
    private void removeFailNode(ServiceMetaInfo currentServiceMetaInfo, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollUtil.isNotEmpty(serviceMetaInfoList)) {
            Iterator<ServiceMetaInfo> iterator = serviceMetaInfoList.iterator();
            while (iterator.hasNext()) {
                ServiceMetaInfo next = iterator.next();
                if (currentServiceMetaInfo.getServiceNodeKey().equals(next.getServiceNodeKey())) {
                    iterator.remove();
                }
            }
        }
    }
}
