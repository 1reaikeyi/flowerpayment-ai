package start.filter;

import common.constant.JwtConstant;
import common.constant.RedisPrefixConstant;
import common.constant.RoleConstant;
import common.properties.JwtProperties;
import common.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import service.security.LoginUserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AdminRefreshRequestFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public AdminRefreshRequestFilter(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            // 没有 token，放行，由后面的认证过滤器决定是否 401
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }
            String adminId = claims.get(JwtConstant.ADMIN_ID).toString();
            String adminName = claims.get(JwtConstant.ADMIN_NAME).toString();
            String type = claims.get(JwtConstant.TYPE).toString();
           
            if (!RoleConstant.ROLE_ADMIN.equals(type)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从 Redis 中查找 admin token（使用正确的 ADMIN_AUTH_PREFIX 前缀）
            String standardToken = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.ADMIN_AUTH_PREFIX + adminId);
            if (!token.equals(standardToken)) {
                log.error("admin Token 验证失败，已注销登录" + adminName);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 滑动过期：admin token 使用 ADMIN_AUTH_PREFIX
            stringRedisTemplate.expire(RedisPrefixConstant.ADMIN_AUTH_PREFIX + adminId,
                    jwtProperties.getAdminTtl(), TimeUnit.SECONDS);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    new LoginUserDetails(Long.parseLong(adminId), adminName,
                            Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN))
                    ),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN))
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // token 已过期 → 401，前端应引导重新登录
            log.error("admin Token 已过期: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            // 签名不匹配，说明不是 emp token（可能是 user token），交给下一个过滤器处理
            log.info("admin 过滤器签名不匹配，交给下一个过滤器: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }

    }
}
