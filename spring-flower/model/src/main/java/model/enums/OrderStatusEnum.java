package model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;

public enum OrderStatusEnum {
    /**
     * 鲜花订单状态（对齐数据库设计文档 flower_order.status）：
     * 1 用户下单
     * 2 确认支付
     * 3 商家制作
     * 4 快递员取货
     * 5 配送中
     * 6 已送达
     * 7 系统自动确认
     * 8 已取消
     * 注：保留原枚举常量名，仅更新业务文案，降低对其它模块的破坏
     */
    ORDER(1L, "用户下单"),
    PAYMENT(2L, "确认支付"),
    COOKING(3L, "商家制作"),
    GO(4L, "快递员取货"),
    DELIVERING(5L, "配送中"),
    ARRIVED(6L, "已送达"),
    COMPLETED(7L, "系统自动确认"),
    CANCELLED(8L, "已取消");
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
