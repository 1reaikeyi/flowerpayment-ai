package model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;

public enum OrderStatusEnum {
    /**
     * 鲜花订单状态（对齐数据库设计文档 flower_order.status）：
     1 用户下单 → 2 用户确认支付 → 3 商家制作 → 4 工作人员取货 → 5 工作人员开始配送 → 6 工作人员已到达 → 7 用户确认
     → 8 已取消（未接单退款、商家拒单、超时取消、退款）
     */
    ORDER(1L, "用户下单"),
    PAYMENT(2L, "用户确认支付"),
    COOKING(3L, "商家制作"),
    GO(4L, "工作人员取货"),
    DELIVERING(5L, "工作人员开始配送"),
    ARRIVED(6L, "工作人员已到达"),
    COMPLETED(7L, "系统自动确认"),
    CANCELLED(8L, "用户确认");
    @EnumValue
    private final Long code;
    private final String text;

    OrderStatusEnum(Long code, String fullText) {
        this.code = code;
        this.text = fullText;
    }

    public Long getCode() {
        return code;
    }
    public String getFullText() {
        return text;
    }

}
