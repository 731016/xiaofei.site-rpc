package site.xiaofei.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import site.xiaofei.RpcApplication;
import site.xiaofei.config.RpcConfig;
import site.xiaofei.constant.RpcConstant;
import site.xiaofei.fault.retry.RetryStrategy;
import site.xiaofei.fault.retry.RetryStrategyFactory;
import site.xiaofei.fault.tolerant.TolerantStrategy;
import site.xiaofei.fault.tolerant.TolerantStrategyFactory;
import site.xiaofei.loadbalancer.LoadBalancer;
import site.xiaofei.loadbalancer.LoadBalancerFactory;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.model.ServiceMetaInfo;
import site.xiaofei.protocol.*;
import site.xiaofei.registry.Registry;
import site.xiaofei.registry.RegistryFactory;
import site.xiaofei.serializer.JdkSerializer;
import site.xiaofei.serializer.Serializer;
import site.xiaofei.serializer.SerializerFactory;
import site.xiaofei.server.tcp.VertxTcpClient;

import javax.xml.ws.Service;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * @author tuaofei
 * @description 服务代理（jdk动态代理）
 * @date 2024/10/18
 */
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        //给rpc框架发送请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .args(args)
                .build();

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        if (rpcConfig == null) {
            throw new RuntimeException("get rpcConfig error");
        }
        //从注册中心获取服务地址
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("not find service address");
        }
        //暂时先取第一个
        //ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
        //负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
        System.out.println("获取节点：" + selectedServiceMetaInfo);

        RpcResponse rpcResponse;
        try {
            //发送http请求
//            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
//            rpcResponse = retryStrategy.doRetry(() -> {
//                byte[] bodyBytes = serializer.serializer(rpcRequest);
//                byte[] resultBytes;
//                String remoteUrl = selectedServiceMetaInfo.getServiceAddress();
//                HttpResponse httpResponse = HttpRequest.post(remoteUrl)
//                        .body(bodyBytes)
//                        .execute();
//                resultBytes = httpResponse.bodyBytes();
//                return serializer.deserializer(resultBytes, RpcResponse.class);
//            });

            //发送tcp请求
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() -> VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo));
        } catch (Exception e) {
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            Map<String, Object> requestTolerantParamMap = new HashMap<>();
            requestTolerantParamMap.put("rpcRequest",rpcRequest);
            requestTolerantParamMap.put("selectedServiceMetaInfo",selectedServiceMetaInfo);
            requestTolerantParamMap.put("serviceMetaInfoList",serviceMetaInfoList);
            rpcResponse = tolerantStrategy.doTolerant(requestTolerantParamMap, e);
        }
        return rpcResponse.getData();
    }
}
