package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鲜花订单明细实体类（对应 flower_order_detail 表）
 * 下单商品明细（鲜花单品 / 节日多花）
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("flower_order_detail")
@Getter
@Setter
@ToString
public class FlowerOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 主订单 ID，关联 flower_order.id
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 商品名称（冗余字段）
     */
    @TableField("name")
    private String name;

    /**
     * 商品图片（冗余字段）
     */
    @TableField("image")
    private String image;

    /**
     * 鲜花单品 ID，购买单品时赋值
     */
    @TableField("flower_id")
    private Long flowerId;

    /**
     * 节日多花 ID，购买多花礼盒时赋值
     */
    @TableField("festival_id")
    private Long festivalId;

    /**
     * 购买数量，默认 1
     */
    @TableField("number")
    private Long number;

    /**
     * 单条明细金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 多花礼盒包装费
     */
    @TableField("wrap_fee")
    private Long wrapFee;
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
}
