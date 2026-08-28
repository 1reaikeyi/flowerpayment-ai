package model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1, message = "页码不能小于1")
    private Long page = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页条数不能超过20")
    private Long pageSize = 10L;

    // 起始时间
    private String startTime;

    // 结束时间
    private String endTime;
}
