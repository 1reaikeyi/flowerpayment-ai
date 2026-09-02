package ai.service.graph.tool;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.Flower;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowerJson {

    @JsonPropertyDescription("鲜花id")
    private Long id;

    @JsonPropertyDescription("鲜花名称")
    private String name;

    @JsonPropertyDescription("所属分类ID")
    private Long categoryId;

    @JsonPropertyDescription("颜色")
    private String color;

    @JsonPropertyDescription("单价，单位为元，货币为人民币")
    private BigDecimal price;

    @JsonPropertyDescription("商品图片URL")
    private String image;

    @JsonPropertyDescription("花语/描述信息")
    private String description;

    @JsonPropertyDescription("售卖状态：0 下架，1 在售")
    private String status;

    @JsonPropertyDescription("创建时间，格式：yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonPropertyDescription("更新时间，格式：yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 从 Flower 实体转为
     */
    public static FlowerJson of(Flower flower) {
        if (null == flower) {
            throw new IllegalArgumentException("没有查询到鲜花数据");
        }
        return FlowerJson.builder()
                .id(flower.getId())
                .name(flower.getName())
                .categoryId(flower.getCategoryId())
                .color(flower.getColor())
                .price(flower.getPrice())
                .image(flower.getImage())
                .description(flower.getDescription())
                .status(flower.getStatus() == 1 ? "在售" : "下架")
                .createTime(flower.getCreateTime())
                .updateTime(flower.getUpdateTime())
                .build();
    }

}
