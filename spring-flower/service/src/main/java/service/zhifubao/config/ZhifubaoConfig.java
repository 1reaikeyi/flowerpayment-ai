package service.zhifubao.config;

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