package model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum PayStatusEnum {
    /**
     * 支付状态：0未支付 1已支付 2退款
     */
    UNPAID(0L, "未支付"),
    PAID(1L, "已支付"),
    REFUNDED(2L, "退款");
    @EnumValue
    private Long status;
    private String name;
    PayStatusEnum(Long status, String name) {
        this.status = status;
        this.name = name;
    }
}
