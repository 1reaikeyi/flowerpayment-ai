package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.UserShoppingDTO;
import model.entity.UserShopping;
import model.vo.UserShoppingVO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


/**
 * 购物车 Service（对应 user_shopping 表）
 */

public interface UserShoppingService extends IService<UserShopping> {
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    void deleteAll();
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    void delete(Long id);
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    List<UserShoppingVO> readAll();
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    UserShoppingDTO create(UserShoppingDTO userShoppingDTO);
}
