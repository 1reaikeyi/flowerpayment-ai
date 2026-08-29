package start.controller.wallet;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.zhifubao.DTO.PayDTO;
import service.zhifubao.config.ZhifubaoProperties;
import service.zhifubao.service.ZhifubaoService;
import service.zhifubao.DTO.RefundDTO;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//全局查询修改
@RestController
@RequestMapping("/pay")
@Slf4j
public class ZhifubaoController {

    @Autowired
    private ZhifubaoService zhifubaoService;
    @Autowired
    private ZhifubaoProperties zhifubaoProperties;

    /** 电脑网站支付：浏览器打开此接口会跳转到支付宝沙箱收银台 */
    @GetMapping("/order")
    public void orderPay(PayDTO payDTO, HttpServletResponse response) throws Exception {
        String form = zhifubaoService.createPagePayForm(payDTO);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
    }
    /** 交易查询 */
    @GetMapping("/order/query")
    public AlipayTradeQueryResponse queryOrder(@RequestParam String outTradeNo) throws Exception {
        return zhifubaoService.queryTrade(outTradeNo);
    }
    /** 退款 */
    @PostMapping("/refund")
    public AlipayTradeRefundResponse refundOrder(RefundDTO refundDTO) throws Exception {
        return zhifubaoService.refund(refundDTO);
    }

    /** 退款查询 */
    @GetMapping("/refund/query")
    public AlipayTradeFastpayRefundQueryResponse refundQuery(@RequestParam String outTradeNo, @RequestParam String outRequestNo) throws Exception {
        return zhifubaoService.refundQuery(outTradeNo, outRequestNo);
    }

    /** 关闭交易 */
    @PostMapping("/order/close")
    public AlipayTradeCloseResponse close(@RequestParam String outTradeNo) throws Exception {
        return zhifubaoService.close(outTradeNo);
    }

    /** 同步跳转 */
    @GetMapping("/return")
    public String returnUrl() {
        return "已返回商户页面,同步返回。";
    }

    /** 异步通知*/
    @PostMapping("/notify")
    public String notifyUrl(HttpServletRequest request) {
        log.info("收到支付宝异步通知");
        Map<String, String> params = new HashMap<>();
        // 1. 转换请求参数
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        try {
            // 2. 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    zhifubaoProperties.getAlipayPublicKey(),
                    zhifubaoProperties.getCharset(),
                    zhifubaoProperties.getSignType()
            );

            if (!signVerified) {
                log.error("支付宝异步通知验签失败, 非法请求。params={}", params);
                return "failure";
            }

            // 3. 基础字段校验
//            String appId = params.get("app_id");
//            String outTradeNo = params.get("out_trade_no"); // 商户订单号
//            String tradeNo = params.get("trade_no");        // 支付宝交易号
//            String tradeStatus = params.get("trade_status");
//            String totalAmount = params.get("total_amount"); // 订单金额
//
//            // 校验 app_id 是否匹配
//            if (appId == null || !appId.equals(alipayConfig.getAppId())) {
//                log.error("通知 app_id 不匹配，预期: {}, 实际: {}", alipayConfig.getAppId(), appId);
//                return "failure";
//            }

            // 4. 业务逻辑处理
//            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
//                // 先查询数据库，看该订单是否已经处理过
//                Order order = orderService.getById(outTradeNo);
//                if (order == null) {
//                    log.error("订单不存在！out_trade_no={}", outTradeNo);
//                    return "failure";
//                }
//
//                // 如果订单状态已经是“已支付”，说明之前已经处理过了，直接返回 success
//                if (OrderStatusEnum.PAID.equals(order.getStatus())) {
//                    log.info("订单已处理过，跳过幂等检查。out_trade_no={}", outTradeNo);
//                    return "success";
//                }
//
//                // --- 步骤 B: 金额校验 (防止篡改金额) ---
//                // 使用 BigDecimal 进行精确比较
//                java.math.BigDecimal notifyAmount = new java.math.BigDecimal(totalAmount);
//                if (notifyAmount.compareTo(order.getAmount()) != 0) {
//                    log.error("订单金额不一致！out_trade_no={}, 通知金额={}, 数据库金额={}", outTradeNo, notifyAmount, order.getTotalAmount());
//                    return "failure";
//                }
//                // 建议开启数据库事务，确保状态更新和后续逻辑的一致性
//                orderService.updateOrderStatus(outTradeNo, OrderStatusEnum.PAID, tradeNo);
//
//                 这里可以发送消息到 MQ 处理非核心逻辑（如发短信、加积分），避免阻塞回调
//                 mqProducer.sendOrderPaidMessage(outTradeNo);
//
//                log.info("支付宝异步通知处理成功，订单号: {}, 交易号: {}", outTradeNo, tradeNo);
//                return "success";
//            }
//
//            // 处理其他状态（如交易关闭）
//            log.info("收到非成功状态通知: {}, 订单号: {}", tradeStatus, outTradeNo);
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝异步通知发生异常", e);
            // 捕获异常返回 failure，让支付宝重试，避免丢失通知
            return "failure";
        }
    }

}

/**
 * 推荐手工测试顺序（沙箱）
 * ① 下单（页面支付）
 * http://localhost:8080/pay/order?outTradeNo=002&amount=0.01&subject=测试
 * 应返回一段 HTML，并自动跳转到支付宝沙箱收银台；用沙箱买家账号完成支付。
 * ② 交易查询（支付前/后都可试）：
 * http://localhost:8080/pay/query?outTradeNo=TEST20260411001
 * ③ 关单 / 退款（需用 Postman 等发 JSON），例如关单：60min自动，可提前
 * POST http://localhost:8080/pay/close
 * Body（JSON）：{"outTradeNo":"TEST20260411001"}
 * 退款类似：POST /pay/refund，字段与 RefundReq 一致（outTradeNo、refundAmount、refundReason、outRequestNo）。
 * 4. 异步通知 /pay/notify 怎么测
 * 支付宝只会向你配置的 notifyUrl 发 POST。本地 localhost 支付宝访问不到，所以需要：
 * 用 内网穿透（ngrok、cpolar 等）把本机映射成公网 HTTPS 地址，并把 notify-url 配成公网地址 + /pay/notify；或
 *     notify-url: http://localhost:8080/pay/notify
 *     return-url: http://localhost:8080/pay/return
 * notify-url: https://你的域名/pay/notify
 * return-url: http://localhost:8080/pay/return（同步仅展示时可继续用本机）
 */
