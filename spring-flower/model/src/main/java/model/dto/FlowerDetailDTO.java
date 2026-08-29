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
public class FlowerDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联鲜花 ID，关联 flower.id
     */
    private Long flowerId;

    /**
     * 送人对象，如：女友、母亲、朋友
     */
    private String specObject;

    /**
     * 用途/场景，如：表白、生日
     */

    private String specOption;
}
