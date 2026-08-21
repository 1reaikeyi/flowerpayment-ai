package model.dto;

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
    // 页码
    private int page;

    // 每页记录数
    private int pageSize;

    // 礼盒名称（模糊查询）
    private String name;
}
