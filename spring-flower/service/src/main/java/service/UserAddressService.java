package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.entity.UserAddress;
import org.springframework.stereotype.Service;

/**
 * 用户地址簿 Service（对应 user_address 表）
 */
@Service
public interface UserAddressService extends IService<UserAddress> {
}
