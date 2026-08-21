package start.controller.zhifubao;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import start.controller.zhifubao.config.ZhifubaoConfig;
import jakarta.servlet.http.HttpServletResponse;
import start.controller.zhifubao.config.ZhifubaoProperties;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/oauth")
public class OAuthLoginController {
    // 新版沙箱授权域名：openauth-sandbox.dl.alipaydev.com
    private static final String AUTHORIZE_URL = "https://openauth-sandbox.dl.alipaydev.com/oauth2/publicAppAuthorize.htm";

    @Autowired
    private ZhifubaoConfig zhifubaoConfig;
    @Autowired
    private ZhifubaoProperties zhifubaoProperties;

    /** ① 生成授权链接并 302 自动跳转到支付宝授权页（浏览器直接访问即可开始授权） */
    @GetMapping("/authorize")
    public void authorize(@RequestParam String redirectUri, HttpServletResponse response) throws IOException {
        String authorizeUrl = AUTHORIZE_URL
                + "?app_id=" + zhifubaoProperties.getAppId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=auth_user";
        // 302 重定向，浏览器自动跳转到支付宝授权页
        response.sendRedirect(authorizeUrl);
    }

    /** ② 回调：auth_code 换 access_token / user_id，并拉取用户资料 */
    @GetMapping("/callback")
    public Map<String, Object> callback(@RequestParam("auth_code") String authCode) throws AlipayApiException {
        if (authCode == null || authCode.isBlank()) {
            throw new IllegalArgumentException("authCode不能为空");
        }
        AlipayClient client = zhifubaoConfig.getAlipayClient();

        // 1. auth_code 换取 access_token
        AlipaySystemOauthTokenRequest tokenReq = new AlipaySystemOauthTokenRequest();
        tokenReq.setCode(authCode);
        tokenReq.setGrantType("authorization_code");
        AlipaySystemOauthTokenResponse tokenResp = client.execute(tokenReq);
        if (!tokenResp.isSuccess()) {
            log.error("支付宝授权换取token失败: msg={}, subMsg={}", tokenResp.getMsg(), tokenResp.getSubMsg());
            throw new IllegalStateException("支付宝授权失败：" + tokenResp.getMsg());
        }

        String accessToken = tokenResp.getAccessToken();
        String openId = tokenResp.getOpenId();
        String refreshToken = tokenResp.getRefreshToken();

        // accessToken 官方有效期 3600秒
        Long accessExpireSec = Long.valueOf(tokenResp.getExpiresIn());
        long accessExpireTime = System.currentTimeMillis() + accessExpireSec * 1000L;

        // refreshToken 官方原始有效期 re_expires_in = 2592000秒（30天），直接使用，不做本地修改
        Long refreshExpireSec = Long.valueOf(tokenResp.getReExpiresIn());
        long refreshExpireTime = System.currentTimeMillis() + refreshExpireSec * 1000L;

        // 2. access_token 获取用户资料
        AlipayUserInfoShareRequest userReq = new AlipayUserInfoShareRequest();
        userReq.putOtherTextParam("auth_token", accessToken);
        AlipayUserInfoShareResponse userResp = client.execute(userReq);

        Map<String, Object> result = new HashMap<>();
        result.put("openId", openId);
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("expiresIn", accessExpireSec);
        result.put("expireTime", accessExpireTime);
        result.put("refreshExpireTime", refreshExpireTime);

        if (userResp.isSuccess()) {
            result.put("nickName", userResp.getNickName());
            result.put("avatar", userResp.getAvatar());
            result.put("gender", userResp.getGender());
            // 建议后续字段名由 isCertified 修改为 email，消除歧义
            result.put("isCertified", userResp.getEmail());
        } else {
            log.error("拉取支付宝用户资料失败 code={} subCode={} subMsg={} msg={}",
                    userResp.getCode(), userResp.getSubCode(), userResp.getSubMsg(), userResp.getMsg());
        }
        return result;
    }
}
/**
 *  ① 生成授权链接（GET）：
 *     http://localhost:8080/oauth/authorize?redirectUri=http://localhost:8080/oauth/callback
 *     后端 302 自动跳转到支付宝授权页：
 *     https://openauth-sandbox.dl.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=xxx&redirect_uri=xxx&scope=auth_user
 *  ② 在授权页用「沙箱买家账号」登录，点击同意授权。
 *  ③ 授权成功后浏览器自动跳回，地址栏变成：
 *     http://localhost:8080/oauth/callback?auth_code=xxxxxx
 */

