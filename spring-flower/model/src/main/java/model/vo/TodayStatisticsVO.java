package model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodayStatisticsVO {

    private Long paidCount;
    private  int orderCount;
    private BigDecimal todayAmount;
}
