package site.xiaofei.protocol;

import lombok.Getter;

/**
 * @author tuaofei
 * @description 协议消息的状态枚举
 * @date 2024/11/4
 */
@Getter
public enum ProtocolMessageStatusEnum {


    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;
    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum statusEnum : ProtocolMessageStatusEnum.values()) {
            if (value == statusEnum.getValue()) {
                return statusEnum;
            }
        }
        return null;
    }
}
