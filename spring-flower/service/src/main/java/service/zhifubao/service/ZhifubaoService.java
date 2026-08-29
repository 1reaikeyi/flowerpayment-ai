package service.zhifubao.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.zhifubao.config.ZhifubaoConfig;
import service.zhifubao.DTO.PayDTO;
import service.zhifubao.DTO.RefundDTO;
import service.zhifubao.config.ZhifubaoProperties;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ZhifubaoService {

    @Autowired
    private ZhifubaoProperties zhifubaoProperties;
    @Autowired
    private ZhifubaoConfig zhifubaoConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AlipayClient getClient() {
        return zhifubaoConfig.getAlipayClient();
    }
    /** 电脑网站支付（沙箱可用）*/
    public String createPagePayForm(PayDTO payDTO) throws AlipayApiException {
        /**
         * 业务参数
         * out_trade_no：商户订单号
         * total_amount：订单金额（单位：元）
         * subject：订单名称
         * product_code：产品码，固定值为：FAST_INSTANT_TRADE_PAY
         */
        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", payDTO.getOutTradeNo());
        bizContent.put("total_amount", payDTO.getTotalAmount().toPlainString());
        bizContent.put("subject", payDTO.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("timeout_express", "60m");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        /**
         * 支付宝异步通知地址
         */
//        request.setNotifyUrl(zhifubaoProperties.getNotifyUrl());
        /**
         * 支付宝同步通知地址
         */
        request.setReturnUrl(zhifubaoProperties.getReturnUrl());
        request.setBizContent(toJson(bizContent));
        AlipayTradePagePayResponse response = getClient().pageExecute(request);
        return response.getBody();
    }

    /**
     *  交易查询
     *  out_trade_no 商户订单号
     */
    public AlipayTradeQueryResponse queryTrade(String outTradeNo) throws AlipayApiException {
        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(toJson(bizContent));
        return getClient().execute(request);
    }

    /**
     * 退款
     * out_trade_no
     * refund_amount
     * out_request_no
     */
    public AlipayTradeRefundResponse refund(RefundDTO refundDTO) throws AlipayApiException {
        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", refundDTO.getOutTradeNo());
        bizContent.put("refund_amount", refundDTO.getRefundAmount().toPlainString());
        if (refundDTO.getOutRefundNo() == null || refundDTO.getOutRefundNo().isBlank()) {
            bizContent.put("out_request_no", new Date().getTime());
        }
        if (refundDTO.getRefundReason() == null || refundDTO.getRefundReason().isBlank()) {
            bizContent.put("refund_reason", "退款");
        }
        bizContent.put("refund_reason", refundDTO.getRefundReason());

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent(toJson(bizContent));
        return getClient().execute(request);
    }

    /**
     * 退款查询
     * out_trade_no
     * out_request_no
     */
    public AlipayTradeFastpayRefundQueryResponse refundQuery(String outTradeNo, String outRequestNo) throws AlipayApiException {
        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("out_request_no", outRequestNo);
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizContent(toJson(bizContent));
        return getClient().execute(request);
    }

    /**
     * 截至时间到了，关闭订单
     * out_trade_no
     * */
    public AlipayTradeCloseResponse close(String outTradeNo) throws AlipayApiException {
        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizContent(toJson(bizContent));
        return getClient().execute(request);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON序列化失败", e);
        }
    }
}
