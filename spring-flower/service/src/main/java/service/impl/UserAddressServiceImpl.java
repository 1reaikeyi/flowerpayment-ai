package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.result.ScrollResult;
import mapper.UserAddressMapper;
import model.dto.UserAddressDTO;
import model.entity.UserAddress;
import org.springframework.stereotype.Service;
import service.UserAddressService;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
    @Override
    public void aeleteAddress(List<Long> ids) {

    }

    @Override
    public UserAddressDTO create(UserAddressDTO userAddressDTO) {
        return null;
    }

    @Override
    public UserAddress readDefaultAddress() {
        return null;
    }

    @Override
    public ScrollResult readPage() {
        return null;
    }

    @Override
    public void updateDefaultAddress(Long id) {

    }

    @Override
    public void updateAddress(UserAddressDTO userAddressDTO) {

    }
}
