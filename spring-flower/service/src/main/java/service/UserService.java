package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.entity.User;
import org.springframework.stereotype.Service;

/**
 * 用户 Service（对应 user 表）
 */

public interface UserService extends IService<User> {
    User findUsername(String username);
}
