package model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * 礼盒鲜花关系实体类（对应 festival_detail 表）
 * 礼盒内包含的具体鲜花明细（一行一条记录，支持 JOIN 查询）
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("festival_detail")
@Getter
@Setter
@ToString
public class FestivalDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联礼盒 ID，关联 festival.id
     */
    @TableField("festival_id")
    private Long festivalId;

    /**
     * 关联鲜花 ID，关联 flower.id（已拆为单行存储，非逗号拼接）
     */
    @TableField("flower_id")
    private Long flowerId;

    /**
     * 送人对象标注：该花在礼盒中的送人对象
     */
    @TableField("spec_object")
    private String specObject;

    /**
     * 用途标注：该花在礼盒中的用途场景
     */
    @TableField("spec_options")
    private String specOptions;
}
