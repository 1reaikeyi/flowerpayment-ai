package service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.result.ScrollResult;
import model.dto.UserAddressDTO;
import model.entity.UserAddress;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 用户地址簿 Service（对应 user_address 表）
 */

public interface UserAddressService extends IService<UserAddress> {

    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    void deleteAddress(List<Long> ids);
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    UserAddressDTO create(UserAddressDTO userAddressDTO);
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    UserAddress readDefaultAddress();
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    ScrollResult<UserAddress> readPage(Long offset, Long current);
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    void updateDefaultAddress(Long id);
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    void updateAddress(UserAddressDTO userAddressDTO);
}
