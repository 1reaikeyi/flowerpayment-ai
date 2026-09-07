package service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.result.ScrollResult;
import model.dto.UserAddressDTO;
import model.entity.UserAddress;

import java.util.List;

/**
 * 用户地址簿 Service（对应 user_address 表）
 */

public interface UserAddressService extends IService<UserAddress> {
    void deleteAddress(List<Long> ids);

    UserAddressDTO create(UserAddressDTO userAddressDTO);

    UserAddress readDefaultAddress();

    ScrollResult<UserAddress> readPage(Long offset, Long current);

    void updateDefaultAddress(Long id);

    void updateAddress(UserAddressDTO userAddressDTO);
}
