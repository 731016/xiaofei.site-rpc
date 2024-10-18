package site.xiaofei.common.model;

import java.io.Serializable;

/**
 * @author tuaofei
 * @description 用户
 * @date 2024/10/17
 */
public class User implements Serializable {

    private static final long serialVersionUID = -5571881012294210153L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
