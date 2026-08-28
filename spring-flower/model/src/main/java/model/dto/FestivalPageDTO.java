package model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 节日礼盒分页查询 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FestivalPageDTO implements Serializable {
    @Min(value = 1, message = "页码不能小于1")
    private Long page = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页条数不能超过20")
    private Long pageSize = 10L;

    // 礼盒名称（模糊查询）
    private String name;
}
