package common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScrollResult {
    /**
     * 数据列表（例如：List<Blog>）
     */
    private List<?> list;

    /**
     * 本次查询最后一条数据的时间戳（也就是下一次查询的 max）
     */
    private Long minTime;

    /**
     * 本次查询最后一条数据在相同时间戳中的偏移量（也就是下一次查询的 offset）
     */
    private Long offset;
}
