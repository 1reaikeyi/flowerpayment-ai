package start.controller.admin;

import common.constant.ShopConstant;
import common.enums.OperationEnum;
import common.result.Result;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import model.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import start.aop.OperationLogging;

@RestController
@RequestMapping("/admin/shop")
public class AdminShopController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping("{status}")
    @Validated
    public Result updateStatus(@PathVariable
                                   @Min(value = 0) @Max(value = 1) Long status) {
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_STATUS, status == 1 ? "营业中" : "已打烊");
        ShopVO shopVO = new ShopVO(status == 1 ? "营业中" : "已打烊");
        return Result.success(shopVO);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result read() {
        String shop = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS);
        ShopVO shopVO = new ShopVO(shop);
        return Result.success(shopVO);
    }
}
