package start.config;

import cn.hutool.json.JSONUtil;
import common.properties.JwtProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import start.filter.EmployeeRefreshRequestFilter;
import start.filter.InformationRequestFilter;
import start.filter.UserRefreshRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //密码加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    //401未认证异常处理
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) throws IOException, ServletException {
                log.debug("未认证请求: {}, 异常: {}", request.getRequestURI(), authException.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("message", "请先登录");
                response.getWriter().write(JSONUtil.toJsonStr(result));
            }
        };
    }
    //403无权限异常处理
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.debug("无权限请求: {}, 异常: {}", request.getRequestURI(), accessDeniedException.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "无权限访问");
            response.getWriter().write(JSONUtil.toJsonStr(result));
        };
    }
    // 配置CORS
    private void crosConfig(CorsConfigurer<HttpSecurity> cors) {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        //拦截所有请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        cors.configurationSource(source);
    }
    /**
     * 配置 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtProperties jwtProperties,
            StringRedisTemplate stringRedisTemplate) throws Exception {
        UserRefreshRequestFilter userRefreshRequestFilter = new UserRefreshRequestFilter(jwtProperties, stringRedisTemplate);
        EmployeeRefreshRequestFilter employeeRefreshRequestFilter = new EmployeeRefreshRequestFilter(jwtProperties, stringRedisTemplate);
        InformationRequestFilter informationRequestFilter = new InformationRequestFilter();
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(this::crosConfig)
                .authorizeHttpRequests(auth -> auth
                        // 登录、注册、退出等接口保持放行（需先于 /user/** 拦截规则匹配）
                        .requestMatchers("/user/register", "/user/login").permitAll()
                        .requestMatchers("/admin/employee/register", "/admin/employee/login").permitAll()
                        // 只拦截,对于测试使用的pay+auth+role，不拦截
                        .requestMatchers("/admin/**", "/user/**").authenticated()
                        // 其他接口全部放行
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // user 刷新过滤器（处理 user token，其他 token 放行）
                .addFilterBefore(userRefreshRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // emp 刷新过滤器（处理 emp token，其他 token 放行）
                .addFilterBefore(employeeRefreshRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // 认证拦截过滤器（在 RefreshFilter 之后）
                .addFilterBefore(informationRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
