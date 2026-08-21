package model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * 鲜花规格实体类（对应 flower_detail 表）
 * 存储鲜花的多维度规格属性（送人对象 + 适用场景）
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("flower_detail")
@Getter
@Setter
@ToString
public class FlowerDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联鲜花 ID，关联 flower.id
     */
    @TableField("flower_id")
    private Long flowerId;

    /**
     * 送人对象，如：女友、母亲、朋友
     */
    @TableField("spec_object")
    private String specObject;

    /**
     * 用途/场景，如：表白、生日、道歉，逗号分隔
     */
    @TableField("spec_options")
    private String specOptions;
}
