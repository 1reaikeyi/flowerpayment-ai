package start.controller.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import common.constant.RedisPrefixConstant;
import common.enums.OperationEnum;
import common.result.Result;
import model.dto.UserShoppingDTO;
import model.vo.UserShoppingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import service.UserShoppingService;
import start.aop.OperationLogging;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/shopping")
public class UserShoppingController {
    @Autowired
    private UserShoppingService userShoppingService;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result create(@RequestBody UserShoppingDTO userShoppingDTO){
        UserShoppingDTO dto = userShoppingService.create(userShoppingDTO);
        return Result.success(dto);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readAll(){
        List<UserShoppingVO> userShoppingVOList = userShoppingService.readAll();
        return Result.success(userShoppingVOList);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result delete(@RequestParam Long id){
        userShoppingService.delete(id);
        return Result.success(id);
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping("/all")
    public Result deleteAll(){
        userShoppingService.deleteAll();
        return Result.success();
    }
}
