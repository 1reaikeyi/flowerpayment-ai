package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.exception.UseAddressFailedException;
import common.result.ScrollResult;
import mapper.UserAddressMapper;
import model.dto.UserAddressDTO;
import model.entity.UserAddress;
import org.springframework.stereotype.Service;
import service.UserAddressService;
import service.security.SecurityContextParam;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    private static final Long LIMIT_NUMBER = 5L;

    @Override
    public UserAddressDTO create(UserAddressDTO userAddressDTO) {
        UserAddress userAddress = BeanUtil.toBean(userAddressDTO, UserAddress.class);
        Long userId = SecurityContextParam.getCurrentUserId();
        if(userAddress.getIsDefault() == 1){
            UserAddress defaultAddress = this.lambdaQuery()
                    .eq(UserAddress::getUserId,userId)
                    .eq(UserAddress::getIsDefault,1)
                    .one();
            defaultAddress.setIsDefault(0L);
            this.updateById(defaultAddress);
        }
        this.save(userAddress);
        UserAddressDTO dto = BeanUtil.toBean(userAddress, UserAddressDTO.class);
        return dto;
    }

    @Override
    public UserAddress readDefaultAddress() {
        Long userId = SecurityContextParam.getCurrentUserId();
        UserAddress defaultAddress = this.lambdaQuery()
                .eq(UserAddress::getUserId,userId)
                .eq(UserAddress::getIsDefault,1).one();
        return defaultAddress;
    }

    @Override
    public ScrollResult<UserAddress> readPage(Long offset, Long current) {
        ScrollResult<UserAddress> scrollResult = new ScrollResult();
        offset = offset == null ? LIMIT_NUMBER : offset;
        Long userId = SecurityContextParam.getCurrentUserId();
        List<UserAddress> userAddressList = this.lambdaQuery()
                .eq(UserAddress::getUserId,userId)
                .last("limit "+offset)
                .list();
        if(userAddressList.size() == 0 || userAddressList == null){
            scrollResult.setList(null);
            scrollResult.setMinTime(0L);
            scrollResult.setOffset(LIMIT_NUMBER);
            return scrollResult;
        }
        scrollResult.setList(userAddressList);
        scrollResult.setMinTime(userAddressList.get(userAddressList.size() - 1).getId());
        scrollResult.setOffset(LIMIT_NUMBER);
        return scrollResult;
    }

    @Override
    public void updateDefaultAddress(Long id) {
        Long userId = SecurityContextParam.getCurrentUserId();
        UserAddress defaultAddress = this.lambdaQuery()
                .eq(UserAddress::getUserId,userId)
                .eq(UserAddress::getIsDefault,1)
                .one();
        defaultAddress.setIsDefault(0L);
        this.updateById(defaultAddress);
        UserAddress userAddress  = this.lambdaQuery()
                .eq(UserAddress::getUserId,userId)
                .eq(UserAddress::getId, id).one();
        userAddress.setIsDefault(1L);
        this.updateById(userAddress);
    }

    @Override
    public void updateAddress(UserAddressDTO userAddressDTO) {
        if (userAddressDTO.getId() == null){
            throw new UseAddressFailedException(ErrorConstant.OPERATION_ERROR);
        }
        if (userAddressDTO.getIsDefault() == 1){
            this.updateDefaultAddress(userAddressDTO.getId());
        }
        LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAddress::getId,userAddressDTO.getId());
        if (userAddressDTO.getConsignee() != null) {
            updateWrapper.set(UserAddress::getConsignee, userAddressDTO.getConsignee());
        }
        if (userAddressDTO.getPhone() != null) {
            updateWrapper.set(UserAddress::getPhone, userAddressDTO.getPhone());
        }
        if (userAddressDTO.getSex() != null) {
            updateWrapper.set(UserAddress::getSex, userAddressDTO.getSex());
        }
        if (userAddressDTO.getProvinceCode() != null) {
            updateWrapper.set(UserAddress::getProvinceCode, userAddressDTO.getProvinceCode());
        }
        if (userAddressDTO.getProvinceName() != null) {
            updateWrapper.set(UserAddress::getProvinceName, userAddressDTO.getProvinceName());
        }
        if (userAddressDTO.getCityCode() != null) {
            updateWrapper.set(UserAddress::getCityCode, userAddressDTO.getCityCode());
        }
        if (userAddressDTO.getCityName() != null) {
            updateWrapper.set(UserAddress::getCityName, userAddressDTO.getCityName());
        }
        if (userAddressDTO.getDistrictCode() != null) {
            updateWrapper.set(UserAddress::getDistrictCode, userAddressDTO.getDistrictCode());
        }
        if (userAddressDTO.getDistrictName() != null) {
            updateWrapper.set(UserAddress::getDistrictName, userAddressDTO.getDistrictName());
        }
        if (userAddressDTO.getDetail() != null) {
            updateWrapper.set(UserAddress::getDetail, userAddressDTO.getDetail());
        }
        if (userAddressDTO.getLabel() != null) {
            updateWrapper.set(UserAddress::getLabel, userAddressDTO.getLabel());
        }
        if (userAddressDTO.getIsDefault() != null) {
            updateWrapper.set(UserAddress::getIsDefault, userAddressDTO.getIsDefault());
        }
        // 执行更新操作
        this.update(updateWrapper);
    }

    @Override
    public void deleteAddress(List<Long> ids) {
        if(CollectionUtil.isEmpty(ids)){
            throw new UseAddressFailedException(ErrorConstant.OPERATION_ERROR);
        }
        this.removeByIds(ids);
    }
}
