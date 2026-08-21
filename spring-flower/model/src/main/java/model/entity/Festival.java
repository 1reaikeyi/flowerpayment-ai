package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 节日多花组合礼盒实体类（对应 festival 表）
 * 节日礼盒/礼品套餐基础信息
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("festival")
@Getter
@Setter
@ToString
public class Festival implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属分类 ID，关联 flower_category.id
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 礼盒名称
     */
    @TableField("name")
    private String name;

    /**
     * 礼盒价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 鲜花总数量，礼盒内花朵总数
     */
    @TableField("number")
    private Long number;

    /**
     * 售卖状态：0 下架，1 在售
     */
    @TableField("status")
    private Long status;

    /**
     * 礼盒描述
     */
    @TableField("description")
    private String description;

    /**
     * 礼盒图片
     */
    @TableField("image")
    private String image;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人 ID
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 修改人 ID
     */
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
