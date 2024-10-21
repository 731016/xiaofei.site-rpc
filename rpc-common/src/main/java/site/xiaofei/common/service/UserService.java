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

    //=====================以下为mock模拟接口 开发测试使用 START=====================
    default short getMockNumber(){
        return 1;
    }

    default boolean getMockBoolean(){
        return true;
    }

    default int getMockInt(){
        return 1;
    }

    default long getMockLong(){
        return 1L;
    }

    default User getMockUser(){
        User user = new User();
        user.setName("测试");
        return user;
    }

    //=====================以下为mock模拟接口 开发测试使用 END=====================
}
