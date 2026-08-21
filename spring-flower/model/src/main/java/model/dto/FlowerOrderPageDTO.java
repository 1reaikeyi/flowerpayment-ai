package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 鲜花订单分页查询 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerOrderPageDTO {
    // 页码
    private int page;

    // 每页记录数
    private int pageSize;

    // 订单状态
    private Integer status;

    // 起始时间
    private String startTime;

    // 结束时间
    private String endTime;
}
