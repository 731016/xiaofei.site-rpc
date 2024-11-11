package site.xiaofei.loadbalancer;

import org.junit.Assert;
import org.junit.Test;
import site.xiaofei.model.ServiceMetaInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuaofei
 * @description 负载均衡测试
 * @date 2024/11/11
 */
public class LoadBalancerTest {

    final LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    @Test
    public void select(){
        Map<String,Object> requestParamMap = new HashMap<>();
        requestParamMap.put("methodName","apple");
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePost(1234);

        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService");
        serviceMetaInfo2.setServiceVersion("1.0");
        serviceMetaInfo2.setServiceHost("xiaofei.site");
        serviceMetaInfo2.setServicePost(80);

        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo1, serviceMetaInfo2);
        ServiceMetaInfo serviceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
        System.out.println(serviceMetaInfo);
        Assert.assertNotNull(serviceMetaInfo);
        serviceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
        System.out.println(serviceMetaInfo);
        Assert.assertNotNull(serviceMetaInfo);
        serviceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
        System.out.println(serviceMetaInfo);
        Assert.assertNotNull(serviceMetaInfo);
    }

}
