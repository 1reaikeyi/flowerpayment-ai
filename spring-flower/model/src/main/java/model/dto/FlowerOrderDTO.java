package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.FlowerOrderDetail;
import model.enums.DeliveryStatusEnum;
import model.enums.OrderStatusEnum;
import model.enums.PayStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 鲜花订单 DTO（对应 flower_order 表的传输对象，附带订单明细）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerOrderDTO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 订单状态：1 用户下单 → 2 确认支付 → 3 商家制作 → 4 快递员取货 → 5 配送中 → 6 已送达 → 7 系统自动确认 → 8 已取消
     */
    private OrderStatusEnum status;

    /**
     * 下单用户 ID
     */
    private Long userId;

    /**
     * 用户名称（冗余字段）
     */
    private String userName;

    /**
     * 收花人
     */
    private String consignee;

    /**
     * 收花人手机号
     */
    private String phone;

    /**
     * 地址 ID
     */
    private Long addressId;

    /**
     * 完整配送地址
     */
    private String address;

    /**
     * 支付状态：0 未支付，1 已支付，2 退款
     */
    private PayStatusEnum payStatus;

    /**
     * 备注信息（可存 AI 生成的贺卡文案）
     */
    private String remark;

    /**
     * 配送方式：0 预约配送，1 立即送出
     */
    private DeliveryStatusEnum deliveryType;

    /**
     * 预约配送日期
     */
    private LocalDate deliveryDate;

    /**
     * 配送开始时间
     */
    private LocalDateTime startDeliveryTime;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 实际送达时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 订单明细列表
     */
    private List<FlowerOrderDetail> flowerOrderDetailList;

}
