package ai.service.graph.tool;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;

@Data
public class FlowerToolParam {
    @ToolParam(description = "鲜花名称，例如：红玫瑰、向日葵")
    private String name;

    @ToolParam(description = "所属分类ID，关联鲜花分类表")
    private Long categoryId;

    @ToolParam(description = "颜色，例如：红色、粉色、白色")
    private String color;

    @ToolParam(description = "单价，单位为元，货币为人民币")
    private BigDecimal price;

    @ToolParam(description = "商品图片URL")
    private String image;

    @ToolParam(description = "花语/描述信息，介绍这束花的含义")
    private String description;

    @ToolParam(description = "售卖状态：0 下架，1 在售，不传则默认在售")
    private Integer status;

}