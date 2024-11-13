package site.xiaofei.fault.tolerant;

import lombok.extern.slf4j.Slf4j;
import site.xiaofei.model.RpcResponse;

import java.util.Map;

/**
 * @author tuaofei
 * @description 快速失败容错
 * @date 2024/11/13
 */
@Slf4j
public class FailFastTolerantStrategy implements TolerantStrategy {


    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException(e);
    }
}
