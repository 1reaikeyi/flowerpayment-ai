package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.properties.JwtProperties;
import mapper.UserMapper;
import model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.UserService;

/**
 * 用户 Service（对应 user 表）
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码加密器 Bean
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;
    // 懒加载：打破 认证管理器 -> 认证提供者 -> 员工Service 的循环依赖
    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager; // 注入认证管理器

    @Override
    public User findUsername(String username) {
        return super.lambdaQuery().eq(User::getUsername, username).one();
    }
}
