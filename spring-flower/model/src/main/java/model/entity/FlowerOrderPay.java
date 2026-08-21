package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import model.enums.PayStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鲜花订单支付记录实体类（对应 flower_order_pay 表）
 * 独立支付流水表，存储支付、退款、取消相关记录
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("flower_order_pay")
@Getter
@Setter
@ToString
public class FlowerOrderPay implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单 ID，关联 flower_order.id
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 下单时间
     */
    @TableField("order_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 支付方式：1 微信，2 支付宝
     */
    @TableField("pay_method")
    private Long payMethod;

    /**
     * 支付状态：0 未支付，1 已支付，2 已退款
     */
    @EnumValue
    @TableField("pay_status")
    private PayStatusEnum payStatus;

    /**
     * 实际支付时间
     */
    @TableField("checkout_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;

    /**
     * 实收总金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 订单取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 取消 / 退款时间
     */
    @TableField("cancel_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 记录创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 操作创建人 ID
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 操作修改人 ID
     */
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
