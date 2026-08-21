package start.controller.zhifubao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundDTO {

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 退款金额（单位：元）
     */
    private BigDecimal refundAmount;
    /**
     * 退款订单号
     */
    private String outRefundNo;
    /**
     * 退款原因
     */
    private String refundReason;

}
