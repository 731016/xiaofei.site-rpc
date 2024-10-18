package site.xiaofei.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.serializer.JdkSerializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author tuaofei
 * @description 服务代理（jdk动态代理）
 * @date 2024/10/18
 */
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //指定序列化器
        JdkSerializer serializer = new JdkSerializer();

        //给rpc框架发送请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .args(args)
                .build();

        try {
            byte[] bodyBytes = serializer.serializer(rpcRequest);
            byte[] resultBytes;
            //todo 地址需要使用注册中心和服务发现机制解决
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute();
            resultBytes = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserializer(resultBytes, RpcResponse.class);
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
