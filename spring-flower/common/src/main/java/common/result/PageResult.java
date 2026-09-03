package common.result;


import lombok.Data;

import java.util.List;

/**
 * 统一分页返回结构
 */
@Data
public class PageResult<T> {
    private long total;       // 总记录数
    private List<T> list;     // 当前页数据
    private long pageNum;     // 当前页码
    private long pageSize;    // 每页条数

}
