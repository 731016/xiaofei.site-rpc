package site.xiaofei.server.tcp;


import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import site.xiaofei.protocol.ProtocolConstant;

/**
 * @author tuaofei
 * @description 装饰着模式（使用recordParser对原有的buffer处理能力进行增强）
 * @date 2024/11/6
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        this.recordParser = initRecordParser(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        //构造RecordParser
        RecordParser recordParser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        recordParser.setOutput(new Handler<Buffer>() {
            //初始化
            int size = -1;
            //一次完整的读取（header + body）
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    //读取消息体长度
                    size = buffer.getInt(13);
                    recordParser.fixedSizeMode(size);
                    //写入头信息
                    resultBuffer.appendBuffer(buffer);
                } else {
                    //写入消息体信息
                    resultBuffer.appendBuffer(buffer);
                    //已拼接完整buffer，执行处理
                    bufferHandler.handle(resultBuffer);
                    //重置一轮
                    recordParser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return recordParser;
    }
}
