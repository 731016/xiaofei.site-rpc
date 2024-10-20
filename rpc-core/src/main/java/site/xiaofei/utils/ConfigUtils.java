package site.xiaofei.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import site.xiaofei.constant.RpcConstant;

/**
 * @author tuaofei
 * @description 配置工具类
 * @date 2024/10/20
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     *
     * @param tClass
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String fileSuffix, String prefix) {
        return loadConfig(tClass, prefix, fileSuffix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     *
     * @param tClass
     * @param prefix
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String fileSuffix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        if (StrUtil.isNotBlank(fileSuffix)) {
            configFileBuilder.append(fileSuffix);
        } else {
            configFileBuilder.append(RpcConstant.DEFAULT_CONFIG_FILESUFFIX);
        }
        Props props = new Props(configFileBuilder.toString());
        props.autoLoad(true);
        return props.toBean(tClass, prefix);
    }
}
