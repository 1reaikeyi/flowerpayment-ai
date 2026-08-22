package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鲜花单品实体类（对应 flower 表）
 * 存储所有单支/单束鲜花基础信息
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("flower")
@Getter
@Setter
@ToString
public class Flower implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 鲜花名称
     */
    @TableField("name")
    private String name;

    /**
     * 所属分类 ID，关联 flower_category.id
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 颜色
     */
    @TableField("color")
    private String color;
    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 商品图片
     */
    @TableField("image")
    private String image;

    /**
     * 花语/描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 售卖状态：0 下架，1 在售
     */
    @TableField("status")
    private Long status;

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
