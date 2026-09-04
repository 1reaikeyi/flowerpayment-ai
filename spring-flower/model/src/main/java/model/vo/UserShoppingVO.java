package model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserShoppingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车记录 ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 鲜花单品 ID
     */
    private Long flowerId;

    /**
     * 节日多花 ID
     */
    private Long festivalId;

    /**
     * 选购数量
     */
    private Long number;

    /**
     * 小计金额
     */
    private BigDecimal amount;

    /**
     * 加入购物车时间（格式化）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
