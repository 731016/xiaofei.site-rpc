package site.xiaofei.consumer;

import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;
import site.xiaofei.proxy.ServiceProxyFactory;

/**
 * @author tuaofei
 * @description 消费者
 * @date 2024/10/17
 */
public class RpcConsumerExample {

    public static void main(String[] args) {
        //静态代理
//        UserService userService = new UserServiceProxy();
        //jdk动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("土澳菲");
        User resultUser = userService.getUser(user);
        if (resultUser != null){
            System.out.println(resultUser.getName());
        }else{
            System.out.println("user is null!");
        }
//        short number = userService.getMockNumber();
//        System.out.println(number);
    }
}
