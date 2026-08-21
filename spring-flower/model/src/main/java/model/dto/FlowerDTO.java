package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.FlowerDetail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 鲜花单品 DTO（对应 flower 表的传输对象，附带规格明细）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerDTO implements Serializable {
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
     * 单价
     */
    private BigDecimal price;

    /**
     * 商品图片
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
     * 鲜花规格明细（送人对象 + 用途场景）
     */
    private List<FlowerDetail> flowerDetails;
}
