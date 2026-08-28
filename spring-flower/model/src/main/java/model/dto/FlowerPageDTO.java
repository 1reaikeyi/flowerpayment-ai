package model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 鲜花单品分页查询 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowerPageDTO implements Serializable {
    @Min(value = 1, message = "页码不能小于1")
    private Long page = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页条数不能超过20")
    private Long pageSize = 10L;

    // 鲜花名称（模糊查询）
    private String name;
}
