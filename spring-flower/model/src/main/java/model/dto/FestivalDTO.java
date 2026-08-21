package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.FestivalDetail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 节日礼盒 DTO（对应 festival 表的传输对象，附带礼盒内鲜花明细）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FestivalDTO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 所属分类 ID
     */
    private Long categoryId;

    /**
     * 礼盒名称
     */
    private String name;

    /**
     * 礼盒价格
     */
    private BigDecimal price;

    /**
     * 鲜花总数量，礼盒内花朵总数
     */
    private Long number;

    /**
     * 售卖状态：0 下架，1 在售
     */
    private Long status;

    /**
     * 礼盒描述
     */
    private String description;

    /**
     * 礼盒图片
     */
    private String image;

    /**
     * 礼盒内鲜花明细
     */
    private List<FestivalDetail> festivalDetails;
}
