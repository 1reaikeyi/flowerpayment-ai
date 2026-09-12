package model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerOrderDetailDTO {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 主订单 ID，关联 flower_order.id
     */

    private Long orderId;

    /**
     * 商品名称
     */

    private String name;

    /**
     * 商品图片
     */

    private String image;

    /**
     * 鲜花单品 ID，购买单品时赋值
     */

    private Long flowerId;

    /**
     * 节日多花 ID，购买多花礼盒时赋值
     */

    private Long festivalId;

    /**
     * 购买数量，默认 1
     */

    private Long number;

    /**
     * 单条明细金额
     */

    private BigDecimal amount;

    /**
     * 多花礼盒包装费
     */

    private Long wrapFee;
    /**
     * 记录创建时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
