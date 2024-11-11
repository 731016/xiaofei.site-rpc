package site.xiaofei.registry;

/**
 * @author tuaofei
 * @description 注册中心key常量
 * @date 2024/10/30
 */
public interface RegistryKeys {

    String ETCD = "etcd";
    String ETCD_REGISTER_SERVER_ADDRESS = "localhost:2380";

    /**
     * 过期时间：单位：秒
     */
    Long ETCD_TTL = 30L;
    /**
     * 续期时间表达式
     */
    String ETCD_RENEWAL_CRON = "*/10 * * * * * ";

    String ZOOKEEPER = "zookeeper";
    String ZOOKEEPER_REGISTER_SERVER_ADDRESS = "localhost:2181";
}
