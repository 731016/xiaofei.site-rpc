package xiaofei.site.examplespringbootprovider;

import org.springframework.stereotype.Service;
import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;
import xiaofei.site.rpc.springboot.starter.annotation.RpcReferance;
import xiaofei.site.rpc.springboot.starter.annotation.RpcService;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/11/14
 */
@Service
@RpcService
public class UseServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        System.out.println("用户名： " + user.getName());
        return user;
    }
}
