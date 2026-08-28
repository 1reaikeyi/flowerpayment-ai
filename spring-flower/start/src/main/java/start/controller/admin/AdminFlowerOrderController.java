package start.controller.admin;

import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.enums.OperationEnum;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;

import model.dto.FlowerOrderPageDTO;
import model.entity.FlowerOrder;
import model.entity.FlowerOrderDetail;
import model.vo.FlowerOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import service.FlowerOrderDetailService;
import service.FlowerOrderPayService;
import service.FlowerOrderService;
import service.FlowerService;
import start.aop.OperationLogging;
import start.controller.zhifubao.DTO.RefundDTO;
import start.controller.zhifubao.service.ZhifubaoService;

import java.util.List;

@RestController
@RequestMapping("/admin/flowerOrder")
@Slf4j
public class AdminFlowerOrderController {
    @Autowired
    private FlowerOrderService flowerOrderService;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam("id") Long id) {
        FlowerOrderVO flowerOrderVO = flowerOrderService.readById(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage(FlowerOrderPageDTO flowerOrderPageDTO) {
        List<FlowerOrderVO> flowerOrderVOList = flowerOrderService.readPage(flowerOrderPageDTO);
        return Result.success(flowerOrderVOList);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("cooking/{id}")
    public Result update3(@PathVariable Long id) {
        flowerOrderService.update3(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("go/{id}")
    public Result update4(@PathVariable Long id) {
        flowerOrderService.update4(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("delivering/{id}")
    public Result update5(@PathVariable Long id) {
        flowerOrderService.update5(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("arrived/{id}")
    public Result update6(@PathVariable Long id) {
        flowerOrderService.update6(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("/complete/{id}")
    public Result update7(@PathVariable Long id) {
        flowerOrderService.update7(id);
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("canceled/{id}")
    public Result update8(@PathVariable Long id) {
        flowerOrderService.update8(id);
        return Result.success();
    }

}
