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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import io.jsonwebtoken.ExpiredJwtException;
import service.security.LoginUserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;



/**
 * emp 员工 Token 刷新与验证过滤器
 */
@Slf4j
public class EmployeeRefreshRequestFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public EmployeeRefreshRequestFilter(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
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
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }
            String empId = claims.get(JwtConstant.EMP_ID).toString();
            String empName = claims.get(JwtConstant.EMP_NAME).toString();
            String type = claims.get(JwtConstant.TYPE).toString();
            if (!RoleConstant.ROLE_EMP.equals(type)) {
                filterChain.doFilter(request, response);
                return;
            }

            String standardToken = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.EMP_AUTH_PREFIX + empId);
            if (!token.equals(standardToken)) {
                log.error("emp Token 验证失败，已注销登录, 员工ID: {}", empId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 滑动过期
            stringRedisTemplate.expire(RedisPrefixConstant.EMP_AUTH_PREFIX + empId,
                    jwtProperties.getAdminTtl(), TimeUnit.SECONDS);

            // 注意：EMP 员工应该被授予 ROLE_EMP 角色，而不是 ROLE_ADMIN
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    new LoginUserDetails(Long.parseLong(empId), empName,
                            Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_EMP))
                    ),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_EMP))
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // token 已过期 → 401，前端应引导重新登录
            log.error("emp Token 已过期: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            // 签名不匹配，说明不是 emp token（可能是 user token），交给下一个过滤器处理
            log.info("emp 过滤器签名不匹配，交给下一个过滤器: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }

    }
}
