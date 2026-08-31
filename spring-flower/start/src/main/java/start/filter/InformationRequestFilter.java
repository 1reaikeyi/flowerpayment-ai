package start.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class InformationRequestFilter extends OncePerRequestFilter {
    /**
     * 跳过不需要认证的公共路径
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OPTIONS 请求（CORS 预检）直接放行
        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            return true;
        }
        String uri = request.getRequestURI();
        // 登录和注册接口不需要认证，直接放行
        if (uri.equals("/user/login") ||
                uri.equals("/user/register") ||
                uri.equals("/employee/login") ||
                uri.equals("/employee/register")||
                uri.equals("/admin/login") ||
                uri.equals("/admin/register")) {
            return true;
        }
        return !uri.startsWith("/admin") &&
                !uri.startsWith("/employee") &&
                !uri.startsWith("/user");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            log.error("登录请求被拦截: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
