package start.controller.admin;

import common.enums.OperationEnum;
import common.result.PageResult;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;

import model.dto.FlowerOrderPageDTO;
import model.enums.OrderStatusEnum;
import model.vo.FlowerOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import service.FlowerOrderService;
import start.aop.OperationLogging;

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
        return Result.success(flowerOrderVO);
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage(FlowerOrderPageDTO flowerOrderPageDTO) {
        PageResult<FlowerOrderVO> flowerOrderVOPageResult = flowerOrderService.readPage(flowerOrderPageDTO);
        return Result.success(flowerOrderVOPageResult);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("cooking/{id}")
    public Result update3(@PathVariable Long id) {
        flowerOrderService.update3(id);
        return Result.success(OrderStatusEnum.COOKING);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("go/{id}")
    public Result update4(@PathVariable Long id) {
        flowerOrderService.update4(id);
        return Result.success(OrderStatusEnum.GO);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("delivering/{id}")
    public Result update5(@PathVariable Long id) {
        flowerOrderService.update5(id);
        return Result.success(OrderStatusEnum.CANCELLED);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("arrived/{id}")
    public Result update6(@PathVariable Long id) {
        flowerOrderService.update6(id);
        return Result.success(OrderStatusEnum.ARRIVED);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("/complete/{id}")
    public Result update7(@PathVariable Long id) {
        flowerOrderService.update7(id);
        return Result.success(OrderStatusEnum.COMPLETED);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("canceled/{id}")
    public Result update8(@PathVariable Long id) {
        flowerOrderService.update8(id);
        return Result.success(OrderStatusEnum.CANCELLED);
    }

}
