package site.xiaofei.protocol;

import cn.hutool.core.util.IdUtil;
import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Test;
import site.xiaofei.constant.RpcConstant;
import site.xiaofei.model.RpcRequest;

import java.io.IOException;

/**
 * @author tuaofei
 * @description 测试编码，解码
 * @date 2024/11/4
 */
public class ProtocolMessageTest {

    @Test
    public void testEncodeAndDecode() throws IOException {
        //构造消息
        ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOLMAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setBodyLength(0);

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("myService");
        rpcRequest.setMethodName("myMethod");
        rpcRequest.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        rpcRequest.setArgs(new Object[]{"aaa","bbb"});
        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);

        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);

        ProtocolMessage<?> message = ProtocolMessageDecoder.decode(encodeBuffer);

        Assert.assertNotNull(message);
    }

}
