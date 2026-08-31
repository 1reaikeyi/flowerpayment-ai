package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 鲜花及节日多花分类实体类（对应 flower_category 表）
 * 统一管理鲜花单品、节日多花、礼品三级分类
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("flower_category")
@Getter
@Setter
@ToString
public class FlowerCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类类型：1=鲜花商品单只，2=节日商品多只，3=礼品
     */
    @TableField("type")
    private Long type;

    /**
     * 排序序号，默认 0
     */
    @TableField("sort")
    private Long sort;

    /**
     * 启用状态：0 禁用，1 启用
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
