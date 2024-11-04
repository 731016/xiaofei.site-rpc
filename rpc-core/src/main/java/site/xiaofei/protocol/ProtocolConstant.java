package site.xiaofei.protocol;

/**
 * @author tuaofei
 * @description 协议常量
 * @date 2024/11/4
 */
public interface ProtocolConstant {
    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 魔数
     */
    byte PROTOCOLMAGIC = 0x1;

    /**
     * 协议版本
     */
    byte PROTOCOL_VERSION = 0x1;
}
