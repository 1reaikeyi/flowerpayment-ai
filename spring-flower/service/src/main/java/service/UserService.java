package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.LoginDTO;
import model.dto.UserDTO;
import model.entity.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * 用户 Service（对应 user 表）
 */

public interface UserService extends IService<User> {

    User findUsername(String username);
    void register(UserDTO userDTO);
    String login(LoginDTO loginDTO);

    void logout();
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    void updateByObject(UserDTO userDTO);
}
