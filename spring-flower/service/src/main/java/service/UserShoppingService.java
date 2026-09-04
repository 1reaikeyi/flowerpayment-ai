package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.UserShoppingDTO;
import model.entity.UserShopping;
import model.vo.UserShoppingVO;

import java.util.List;


/**
 * 购物车 Service（对应 user_shopping 表）
 */

public interface UserShoppingService extends IService<UserShopping> {
    void deleteAll();

    void delete(Long id);

    List<UserShoppingVO> readAll();

    UserShoppingDTO create(UserShoppingDTO userShoppingDTO);
}
