package model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FestivalDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */

    private Long id;

    /**
     * 关联多花礼盒 ID，关联 festival.id
     */

    private Long festivalId;

    /**
     * 关联鲜花 ID，关联 flower.id
     */

    private Long flowerId;

    /**
     * 鲜花数量：该鲜花在多花组合中的数量
     */

    private Long specNumber;

    /**
     * 送人对象标注：该花在多花组合中的送人对象
     */

    private String specObject;

    /**
     * 用途标注：该花在多花礼盒中的用途场景
     */

    private String specOption;
}
