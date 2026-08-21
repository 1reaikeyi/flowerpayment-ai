package model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 鲜花单品销量统计 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerStatisticsVO {
    private Long flowerId;
    private String name;
    private Long number;
    private BigDecimal amount;
}
