//package start.controller.user;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.json.JSONUtil;
//import common.constant.RedisPrefixConstant;
//import common.result.Result;
//import model.dto.OrderShoppingDTO;
//import model.entity.OrderShopping;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.web.bind.annotation.*;
//import start.aop.OperationLogging;
//import start.security.SecurityContextParam;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/user/shopping")
//public class ShoppingCartController {
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @OperationLogging
//    @PostMapping
//    public Result create(@RequestBody OrderShoppingDTO orderShoppingDTO){
//        Long userId = SecurityContextParam.getCurrentUserId();
//        BoundHashOperations<String, String, String> shoppingCart = stringRedisTemplate
//                .boundHashOps(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId);
//        OrderShopping orderShopping = BeanUtil.toBean(orderShoppingDTO, OrderShopping.class);
//        shoppingCart.put(orderShopping.getId().toString(), JSONUtil.toJsonStr(orderShopping));
//        return Result.success();
//    }
//    @OperationLogging
//    @GetMapping
//    public Result readAll(){
//        Long userId = SecurityContextParam.getCurrentUserId();
//        BoundHashOperations<String, Object, Object> map = stringRedisTemplate
//                .boundHashOps(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId);
//
//        return Result.success();
//    }
//    // 删除单个购物车项：DELETE /user/shopping?id=xxx
//    @OperationLogging
//    @DeleteMapping
//    public Result delete(@RequestParam Long id){
//        Long userId = SecurityContextParam.getCurrentUserId();
//        stringRedisTemplate.boundHashOps(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId)
//                .delete(id.toString());
//        return Result.success();
//    }
//    // 清空当前用户的购物车：DELETE /user/shopping/all
//    @OperationLogging
//    @DeleteMapping("/all")
//    public Result deleteAll(){
//        Long userId = SecurityContextParam.getCurrentUserId();
//        stringRedisTemplate.delete(RedisPrefixConstant.SHOPPING_CART_PREFIX+userId);
//        return Result.success();
//    }
//}
