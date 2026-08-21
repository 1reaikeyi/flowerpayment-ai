package model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum DeliveryStatusEnum {
    /**
     * 配送方式（对齐数据库设计文档 flower_order.delivery_type）：
     * 0 预约配送
     * 1 立即送出
     */
    NOW(1L, "立即送出"),
    BOOK_TIME(0L, "预约配送");
    @EnumValue
    private Long value;
    private String name;
    DeliveryStatusEnum(Long value, String name) {
        this.name = name;
        this.value = value;
    }
}
