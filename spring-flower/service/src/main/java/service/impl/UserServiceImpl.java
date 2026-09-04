package service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.constant.JwtConstant;
import common.constant.RedisPrefixConstant;
import common.constant.RoleConstant;
import common.exception.LoginFailedException;
import common.exception.UserFailedException;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import mapper.UserMapper;
import model.dto.LoginDTO;
import model.dto.UserDTO;
import model.entity.Employee;
import model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.UserService;
import service.security.SecurityContextParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户 Service（对应 user 表）
 */
@Service
@Transactional(rollbackFor = Exception.class)
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

    @Override
    public void register(UserDTO userDTO) {
        User user = this.findUsername(userDTO.getUsername());
        if (user != null) {
            throw new UserFailedException(ErrorConstant.USERNAME_EXIST);
        }
        if(user.getPassword() == null) {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        User user1 = BeanUtil.copyProperties(userDTO, User.class);
        user1.setPassword(user1.getPassword());
        save(user1);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password,
                Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_USER)));
        authenticationManager.authenticate(authenticationToken);

        User user = this.findUsername(username);
        Map<String,Object> map = new HashMap<>();
        map.put(JwtConstant.USER_ID, user.getId());
        map.put(JwtConstant.USER_NAME, user.getUsername());
        map.put(JwtConstant.TYPE,RoleConstant.ROLE_USER);

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), map);
        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.USER_AUTH_PREFIX+ user.getId(), token,
                jwtProperties.getAdminTtl(), TimeUnit.SECONDS);
        return token;
    }

    @Override
    public void logout() {
        // 获取当前登录用户ID
        Long userId = SecurityContextParam.getCurrentUserId();
        if (userId == null) {
            throw new LoginFailedException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        stringRedisTemplate.delete(RedisPrefixConstant.USER_AUTH_PREFIX + userId);
        SecurityContextHolder.clearContext();
    }

    @Override
    public void updateByObject(UserDTO userDTO) {

    }
}
