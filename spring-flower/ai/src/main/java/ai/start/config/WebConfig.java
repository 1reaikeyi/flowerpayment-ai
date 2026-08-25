package ai.start.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ai.start.interceptor.SensitiveWordInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SensitiveWordInterceptor sensitiveWordInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册敏感词拦截器，拦截AI相关接口
        registry.addInterceptor(sensitiveWordInterceptor).addPathPatterns("/ai");
    }
}
