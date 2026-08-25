package ai.start.interceptor;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class SensitiveWordInterceptor implements HandlerInterceptor {

    @Autowired
    private SensitiveWordBs sensitiveWordBs;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String message = request.getParameter("message");
        if (message != null && !message.isEmpty()) {
            boolean containsSensitiveWord = sensitiveWordBs.contains(message);
            if (containsSensitiveWord) {
                log.info("检测到敏感词--- uri: {}, param: {}", request.getRequestURI(), message);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("请求包含敏感词，请修改后重试");
                return false;
            }
        }
        return true;
    }
}