package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类（对应 user_shopping 表）
 * 用户未下单前的临时选购数据
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_shopping")
@Getter
@Setter
@ToString
public class UserShopping implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称，鲜花或礼盒名称
     */
    @TableField("name")
    private String name;

    /**
     * 商品图片
     */
    @TableField("image")
    private String image;

    /**
     * 用户 ID，关联 user.id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 鲜花单品 ID，选购单品时赋值
     */
    @TableField("flower_id")
    private Long flowerId;

    /**
     * 节日礼盒 ID，选购礼盒时赋值
     */
    @TableField("festival_id")
    private Long festivalId;

    /**
     * 选购数量，默认 1
     */
    @TableField("number")
    private Long number;

    /**
     * 小计金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 加入购物车时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
