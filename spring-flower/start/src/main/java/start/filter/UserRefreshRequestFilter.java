package start.filter;

import common.constant.JwtConstant;
import common.constant.RedisPrefixConstant;
import common.constant.RoleConstant;
import common.properties.JwtProperties;
import common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import start.security.LoginUserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;



/**
 * user 用户 Token 刷新与验证过滤器

 */
@Slf4j
public class UserRefreshRequestFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public UserRefreshRequestFilter(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
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
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);
        if (token == null) {
            // 没有 token，放行，由后面的认证过滤器决定是否 401
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }
            String userId = claims.get(JwtConstant.USER_ID).toString();
            String userName = claims.get(JwtConstant.USER_NAME).toString();
            String type = claims.get(JwtConstant.TYPE) != null
                    ? claims.get(JwtConstant.TYPE).toString() : "user";
            if (!RoleConstant.ROLE_USER.equals(type)) {
                filterChain.doFilter(request, response);
                return;
            }
            String standardToken = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.USER_AUTH_PREFIX + userId);
            if (!token.equals(standardToken)) {
                log.error("user Token 验证失败，已注销登录, user ID: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 滑动过期
            stringRedisTemplate.expire(RedisPrefixConstant.USER_AUTH_PREFIX + userId,
                    jwtProperties.getUserTtl(), TimeUnit.SECONDS);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    new LoginUserDetails(Long.parseLong(userId),userName,
                            Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_USER))
                    ),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_USER))
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // token 已过期 → 401，前端应引导重新登录
            log.error("user Token 已过期: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            // 签名不匹配，说明不是 user token（可能是 emp token），交给下一个过滤器处理
            log.debug("user 过滤器签名不匹配，交给下一个过滤器: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }

    }
}
