package model.enums;


import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageTypeEnum {
    USER(0, "用户提问"), ASSISTANT(1, "AI的回答");

    private final int value;
    private final String description;

    MessageTypeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
