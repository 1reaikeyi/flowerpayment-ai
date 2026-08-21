package start.controller.zhifubao.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZhifubaoConfig {
    @Autowired
    private ZhifubaoProperties zhifubaoProperties;

    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                zhifubaoProperties.getGatewayUrl(),
                zhifubaoProperties.getAppId(),
                zhifubaoProperties.getAppPrivateKey(),
                zhifubaoProperties.getFormat(),
                zhifubaoProperties.getCharset(),
                zhifubaoProperties.getAlipayPublicKey(),
                zhifubaoProperties.getSignType()
        );
    }
}
/**
 * 推荐手工测试顺序（沙箱）
 * ① 下单（页面支付）
 * http://localhost:8080/pay/order?outTradeNo=002&amount=0.01&subject=测试
 * 应返回一段 HTML，并自动跳转到支付宝沙箱收银台；用沙箱买家账号完成支付。
 * ② 交易查询（支付前/后都可试）：
 * http://localhost:8080/pay/query?outTradeNo=TEST20260411001
 * ③ 关单 / 退款（需用 Postman 等发 JSON），例如关单：
 * POST http://localhost:8080/pay/close
 * Body（JSON）：{"outTradeNo":"TEST20260411001"}
 * 退款类似：POST /pay/refund，字段与 RefundReq 一致（outTradeNo、refundAmount、refundReason、outRequestNo）。
 * 4. 异步通知 /pay/notify 怎么测
 * 支付宝只会向你配置的 notifyUrl 发 POST。本地 localhost 支付宝访问不到，所以需要：
 * 用 内网穿透（ngrok、cpolar 等）把本机映射成公网 HTTPS 地址，并把 notify-url 配成公网地址 + /pay/notify；或
 * 部署到公网服务器再测。
 * 重要：路径要和 Controller 一致。
 * 你当前 application-dev.yml 里是：
 *     notify-url: http://localhost:8080/pay/notify
 *     return-url: http://localhost:8080/pay/return
 * notify-url: https://你的域名/pay/notify
 * return-url: http://localhost:8080/pay/return（同步仅展示时可继续用本机）
 */
