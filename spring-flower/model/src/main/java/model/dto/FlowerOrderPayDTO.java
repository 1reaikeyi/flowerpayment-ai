package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.PayStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鲜花订单支付记录 DTO（对应 flower_order_pay 表的传输对象）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerOrderPayDTO {
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 关联订单 ID
     */
    private Long orderId;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 支付方式：1 微信，2 支付宝
     */
    private Long payMethod;

    /**
     * 支付状态：0 未支付，1 已支付，2 已退款
     */
    private PayStatusEnum payStatus;

    /**
     * 实际支付时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 实收总金额
     */
    private BigDecimal amount;

    /**
     * 支付备注
     */
    private String remark;

    /**
     * 订单取消原因
     */
    private String cancelReason;

    /**
     * 取消 / 退款时间
     */
    private LocalDateTime cancelTime;

}
