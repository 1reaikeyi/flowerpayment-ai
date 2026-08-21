package start.controller.zhifubao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class ZhifubaoProperties {
    /**
     * 沙箱网关固定为：https://openapi-sandbox.dl.alipaydev.com/gateway.do
     * 生产网关一般为：https://openapi.alipay.com/gateway.do
     */
    private String gatewayUrl;

    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;

    /** SDK格式：JSON */
    private String format = "JSON";

    /** 字符集：UTF-8 */
    private String charset = "UTF-8";

    /** 签名类型：RSA2 */
    private String signType = "RSA2";

    private String notifyUrl;
    private String returnUrl;
}
