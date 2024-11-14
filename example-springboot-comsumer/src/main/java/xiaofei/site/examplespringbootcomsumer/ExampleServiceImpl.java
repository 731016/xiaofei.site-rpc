package xiaofei.site.examplespringbootcomsumer;

import org.springframework.stereotype.Service;
import site.xiaofei.common.model.User;
import site.xiaofei.common.service.UserService;
import xiaofei.site.rpc.springboot.starter.annotation.RpcReferance;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/11/14
 */
@Service
public class ExampleServiceImpl {

    @RpcReferance
    private UserService userService;

    public void test(){
        User user = new User();
        user.setName("xiaofei.site");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }

}
