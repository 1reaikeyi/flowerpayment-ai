package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.RedisPrefixConstant;
import mapper.UserShoppingMapper;
import model.dto.UserShoppingDTO;
import model.entity.UserShopping;
import model.vo.UserShoppingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import service.UserShoppingService;
import service.security.SecurityContextParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserShoppingServiceImpl extends ServiceImpl<UserShoppingMapper, UserShopping> implements UserShoppingService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void deleteAll() {
        Long userId = SecurityContextParam.getCurrentUserId();
        stringRedisTemplate.delete(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId);
    }

    @Override
    public void delete(Long id) {
        Long userId = SecurityContextParam.getCurrentUserId();
        stringRedisTemplate.boundHashOps(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId)
                .delete(id.toString());
    }

    @Override
    public List<UserShoppingVO> readAll() {
        Long userId = SecurityContextParam.getCurrentUserId();
        BoundHashOperations<String, Object, Object> map = stringRedisTemplate
                .boundHashOps(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId);
        List<Object> values = map.values();
        if (CollectionUtil.isEmpty(values)) {
            return null;
        }
        List<UserShoppingVO> resultList = new ArrayList<>();
        for (Object value : values) {
            UserShopping userShopping = JSONUtil.toBean(value.toString(), UserShopping.class);
            UserShoppingVO vo = BeanUtil.toBean(userShopping, UserShoppingVO.class);
            resultList.add(vo);
        }
        return resultList;
    }

    @Override
    public UserShoppingDTO create(UserShoppingDTO userShoppingDTO) {
        Long userId = SecurityContextParam.getCurrentUserId();
        BoundHashOperations<String, String, String> shoppingCart = stringRedisTemplate
                .boundHashOps(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId);
        UserShopping userShopping = BeanUtil.toBean(userShoppingDTO,UserShopping.class);
        shoppingCart.put(userShopping.getId().toString(), JSONUtil.toJsonStr(userShopping));
        return userShoppingDTO;
    }
}
