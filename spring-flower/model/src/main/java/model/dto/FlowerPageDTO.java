package model.dto;

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
    // 页码
    private int page;

    // 每页记录数
    private int pageSize;

    // 鲜花名称（模糊查询）
    private String name;
}
