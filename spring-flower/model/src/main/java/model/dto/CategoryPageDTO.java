package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPageDTO implements Serializable {
    //页码
    private int page;

    //每页记录数
    private int pageSize;

    private String name;

    private Long type;
}
