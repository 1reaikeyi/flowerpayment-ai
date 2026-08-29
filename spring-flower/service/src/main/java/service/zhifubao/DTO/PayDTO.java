package service.zhifubao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayDTO {
    /**
     * 商户订单号
     */
    private String outTradeNo;
    /**
     * 订单金额（单位：元）
     */
    private BigDecimal totalAmount;
    /**
     * 订单名称
     */
    private String subject;

}
