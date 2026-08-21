package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.UserMapper;
import model.entity.User;
import org.springframework.stereotype.Service;
import service.UserService;

/**
 * 用户 Service（对应 user 表）
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findUsername(String username) {
        return super.lambdaQuery().eq(User::getUsername, username).one();
    }
}
