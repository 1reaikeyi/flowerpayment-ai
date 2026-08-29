package start.controller.statistics;

import common.enums.OperationEnum;
import common.result.Result;
import model.entity.*;
import model.enums.OrderStatusEnum;
import model.enums.PayStatusEnum;
import model.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.*;
import start.aop.OperationLogging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("admin/statistics")
public class Statistics {

    private static final Integer TOP_NUMBER = 7;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/flower")
    public Result flowerSale() {

        return Result.success();
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/festival")
    public Result festivalSale() {
        return Result.success();
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/top1")
    public Result top1() {
        return Result.success();
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/top2")
    public Result top2() {
        return Result.success();
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/order")
    public Result order() {

        return Result.success();
    }

    // 今日统计：今日订单数、已支付订单数、今日营业额
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/today")
    public Result today() {
        return Result.success();
    }
}
