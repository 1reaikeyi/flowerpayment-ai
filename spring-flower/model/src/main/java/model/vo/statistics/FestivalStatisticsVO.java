package model.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 节日礼盒销量统计 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FestivalStatisticsVO {
    private Long festivalId;
    private String name;
    private Long number;
    private BigDecimal amount;
}
