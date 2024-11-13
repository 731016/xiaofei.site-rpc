package site.xiaofei.provider;

import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;

/**
 * @author tuaofei
 * @description 服务提供者
 * @date 2024/10/17
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        if (user == null){
            return null;
        }
        System.out.println(String.format("用户名：%s",user.getName()));
        int i = 1/0;
        return user;
    }
}
