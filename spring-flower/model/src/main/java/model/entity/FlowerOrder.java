package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import model.enums.DeliveryStatusEnum;
import model.enums.OrderStatusEnum;
import model.enums.PayStatusEnum;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 鲜花订单主表实体类（对应 flower_order 表）
 * 存储订单整体状态、收货配送信息
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("flower_order")
@Getter
@Setter
@ToString
public class FlowerOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单状态：
     * 1 用户下单 → 2 确认支付 → 3 商家制作 → 4 快递员取货 → 5 配送中 → 6 已送达 → 7 系统自动确认 → 8 已取消
     */
    @EnumValue
    @TableField("status")
    private OrderStatusEnum status;

    /**
     * 下单用户 ID，关联 user.id
     */
    @TableField(value = "user_id", fill = FieldFill.INSERT)
    private Long userId;

    /**
     * 用户名称（冗余字段）
     */
    @TableField("username")
    private String userName;

    /**
     * 收花人（冗余字段）
     */
    @TableField("consignee")
    private String consignee;

    /**
     * 收花人手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 地址 ID，关联 user_address.id
     */
    @TableField("address_id")
    private Long addressId;

    /**
     * 完整配送地址（冗余字段）
     */
    @TableField("address")
    private String address;

    /**
     * 支付状态：0 未支付，1 已支付，2 退款
     */
    @EnumValue
    @TableField("pay_status")
    private PayStatusEnum payStatus;

    /**
     * 备注信息，可存 AI 生成的贺卡文案
     */
    @TableField("remark")
    private String remark;

    /**
     * 配送方式：0 预约配送，1 立即送出
     */
    @EnumValue
    @TableField("delivery_type")
    private DeliveryStatusEnum deliveryType;

    /**
     * 预约配送日期（鲜花核心字段）
     */
    @TableField("delivery_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;

    /**
     * 配送开始时间
     */
    @TableField("start_delivery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDeliveryTime;

    /**
     * 预计送达时间
     */
    @TableField("estimated_delivery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 实际送达时间
     */
    @TableField("delivery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
