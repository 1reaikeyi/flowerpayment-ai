package model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FestivalVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属分类 ID
     */
    private Long categoryId;


    /**
     * 多花名称
     */
    private String name;

    /**
     * 多花礼盒价格
     */
    private BigDecimal price;

    /**
     * 鲜花总数量
     */
    private Long number;

    /**
     * 售卖状态：0 下架，1 在售
     */
    private Long status;

    /**
     * 售卖状态描述（如："在售"、"已下架"）
     */
    private String statusDesc;

    /**
     * 多花礼盒描述
     */
    private String description;

    /**
     * 多花礼盒图片 URL
     */
    private String image;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
