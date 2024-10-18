package site.xiaofei.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;
import site.xiaofei.model.RpcRequest;
import site.xiaofei.model.RpcResponse;
import site.xiaofei.serializer.JdkSerializer;

import java.io.IOException;

/**
 * @author tuaofei
 * @description 静态代理
 * @date 2024/10/18
 */
public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        //指定序列化器
        JdkSerializer serializer = new JdkSerializer();

        //给rpc框架发送请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .paramTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();

        try {
            byte[] bodyBytes = serializer.serializer(rpcRequest);
            byte[] resultBytes;
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute();
            resultBytes = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserializer(resultBytes, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
