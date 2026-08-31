package start.controller.statistics;

import common.enums.OperationEnum;
import common.result.Result;
import model.vo.statistics.StatisticsVO;
import model.vo.statistics.OrderStatisticsVO;
import model.vo.statistics.TopStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.*;
import start.aop.OperationLogging;

import java.util.*;

@RestController
@RequestMapping("admin/statistics")
public class Statistics {

    @Autowired
    private FlowerOrderDetailService flowerOrderDetailService;

    private static final Integer TOP_NUMBER = 7;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/flower")
    public Result flowerSale() {
        List<StatisticsVO> statisticsVOList = flowerOrderDetailService.flowerSale();
        return Result.success(statisticsVOList);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/festival")
    public Result festivalSale() {
        List<StatisticsVO> festivalStatisticsVOList = flowerOrderDetailService.festivalSale();
        return Result.success(festivalStatisticsVOList);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/top1")
    public Result top1() {
        List<TopStatisticsVO> statisticsVOList = flowerOrderDetailService.top1();
        return Result.success(statisticsVOList);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/top2")
    public Result top2() {
        List<TopStatisticsVO> festivalStatisticsVOList = flowerOrderDetailService.top2();
        return Result.success(festivalStatisticsVOList);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/order")
    public Result order() {
        List<OrderStatisticsVO> orderStatisticsVOList = flowerOrderDetailService.order();
        return Result.success(orderStatisticsVOList);
    }

    // 今日统计：今日订单数、已支付订单数、今日营业额
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/today")
    public Result today() {
        List<OrderStatisticsVO> orderStatisticsVOList = flowerOrderDetailService.todayOrder();
        return Result.success(orderStatisticsVOList);
    }
}
