package site.xiaofei.fault.tolerant;

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
import java.util.List;
import java.util.Map;

/**
 * @author tuaofei
 * @description 故障转移容错
 * @date 2024/11/13
 */
public class FailOverTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //获取其它节点并调用
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceMetaInfoList");

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
        System.out.println("获取节点：" + selectedServiceMetaInfo);

        RpcResponse rpcResponse;
        try {
            //发送tcp请求
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() -> VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return rpcResponse;
    }
}
