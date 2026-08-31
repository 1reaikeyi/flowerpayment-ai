package model.vo.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 鲜花订单状态统计 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatisticsVO {
    private Long status;
    private String name;
    private Long count;
}
