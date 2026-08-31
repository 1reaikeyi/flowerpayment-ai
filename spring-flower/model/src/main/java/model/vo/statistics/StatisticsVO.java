package model.vo.statistics;

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
public class StatisticsVO {
    private Long id;
    private String name;
    private Long count;
    private BigDecimal totalAccount;
}
