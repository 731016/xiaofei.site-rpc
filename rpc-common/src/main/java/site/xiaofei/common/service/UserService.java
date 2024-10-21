package site.xiaofei.common.service;

import site.xiaofei.common.model.User;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/10/17
 */
public interface UserService {

    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 获取数字
     * @return
     */
    default short getNumber(){
        return 1;
    }
}
