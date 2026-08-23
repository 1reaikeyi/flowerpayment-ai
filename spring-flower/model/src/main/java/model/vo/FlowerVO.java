package model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.FlowerDetail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 鲜花名称
     */
    private String name;

    /**
     * 所属分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称（关联 flower_category 表查询，前端展示用）
     */
    private String categoryName;

    /**
     * 颜色
     */
    private String color;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 商品图片 URL
     */
    private String image;

    /**
     * 花语/描述信息
     */
    private String description;

    /**
     * 售卖状态：0 下架，1 在售
     */
    private Long status;

    /**
     * 售卖状态描述（如："在售"、"已下架"）
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
