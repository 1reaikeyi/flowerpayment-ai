package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 鲜花分类 DTO（对应 flower_category 表的传输对象）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerCategoryDTO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 分类类型：1=鲜花商品单只，2=节日商品多只，3=礼品
     */
    private Long type;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序序号
     */
    private Long sort;

    /**
     * 启用状态：0 禁用，1 启用
     */
    private Long status;

}
