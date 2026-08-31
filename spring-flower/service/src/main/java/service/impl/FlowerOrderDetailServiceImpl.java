package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.result.Result;
import mapper.FlowerOrderDetailMapper;
import model.entity.FlowerOrderDetail;
import model.vo.FestivalVO;
import model.vo.FlowerVO;
import model.vo.statistics.StatisticsVO;
import model.vo.statistics.OrderStatisticsVO;
import model.vo.statistics.TopStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.FestivalService;
import service.FlowerService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowerOrderDetailServiceImpl extends ServiceImpl<FlowerOrderDetailMapper, FlowerOrderDetail> implements service.FlowerOrderDetailService {

    @Autowired
    private FlowerService flowerService;
    @Autowired
    private FestivalService festivalService;

    private static final int TOP_NUMBER = 7;
    @Override
    public List<StatisticsVO> flowerSale() {
        List<FlowerOrderDetail> details = super.lambdaQuery()
                .isNotNull(FlowerOrderDetail::getFlowerId)
                .list();
        List<StatisticsVO> statisticsVOList = new ArrayList<>();
        Map<Long, Long> numberMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (FlowerOrderDetail d : details) {
            numberMap.merge(d.getFlowerId(), d.getNumber(), Long::sum);
            amountMap.merge(d.getFlowerId(), d.getAmount(), BigDecimal::add);
        }
        // 组装结果
        numberMap.forEach((id, number) -> {
            StatisticsVO statisticsVO = new StatisticsVO();
            FlowerVO flowerVO = flowerService.readCache(id);
            statisticsVO.setName(flowerVO != null ? flowerVO.getName() : null);
            statisticsVO.setId(flowerVO != null ? flowerVO.getId() : null);
            statisticsVO.setCount(number);
            statisticsVO.setTotalAccount(amountMap.getOrDefault(flowerVO, BigDecimal.ZERO));
            statisticsVOList.add(statisticsVO);
        });
        statisticsVOList.sort((a, b) -> Long.compare(b.getCount(),a.getCount()));
        return statisticsVOList;
    }

    @Override
    public List<StatisticsVO> festivalSale() {
        List<FlowerOrderDetail> details = super.lambdaQuery()
                .isNotNull(FlowerOrderDetail::getFestivalId)
                .list();
        Map<Long, Long> numberMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        for (FlowerOrderDetail d : details) {
            numberMap.merge(d.getFestivalId(), d.getNumber(), Long::sum);
            amountMap.merge(d.getFestivalId(), d.getAmount(), BigDecimal::add);
        }
        List<StatisticsVO> statisticsVOList = new ArrayList<>();
        numberMap.forEach((id, number) -> {
            StatisticsVO statisticsVO = new StatisticsVO();
            FestivalVO festivalVO = festivalService.readCache(id);
            statisticsVO.setName(festivalVO != null ? festivalVO.getName() : null);
            statisticsVO.setId(festivalVO != null ? festivalVO.getId() : null);
            statisticsVO.setCount(number);
            statisticsVO.setTotalAccount(amountMap.getOrDefault(festivalVO, BigDecimal.ZERO));
            statisticsVOList.add(statisticsVO);
        });
        statisticsVOList.sort((a, b) -> Long.compare(b.getCount(),a.getCount()));
        return statisticsVOList;
    }

    @Override
    public List<TopStatisticsVO> top1() {
        List<FlowerOrderDetail> details = super.lambdaQuery()
                .isNotNull(FlowerOrderDetail::getFlowerId)
                .list();

        Map<Long, Long> numberMap = new HashMap<>();
        for (FlowerOrderDetail d : details) {
            numberMap.merge(d.getFlowerId(), d.getNumber(), Long::sum);
        }
        // 降序排序取前
        List<TopStatisticsVO> topStatisticsVOList = new ArrayList<>();

        return null;
    }

    @Override
    public List<TopStatisticsVO> top2() {
        return List.of();
    }


    @Override
    public List<OrderStatisticsVO> order() {
//        List<Order> orders = orderService.list();
//        // 按状态分组计数
//        Map<OrderStatusEnum, Long> countMap = orders.stream()
//                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
//        // 遍历所有枚举值，保证每种状态都返回（数量为 0 也展示）
//        List<OrderStatisticsVO> orderStatisticsVOList = new ArrayList<>();
//        for (OrderStatusEnum status : OrderStatusEnum.values()) {
//            OrderStatisticsVO orderStatistics = new OrderStatisticsVO();
//            orderStatistics.setStatus(status.getCode());
//            orderStatistics.setName(status.getFullText());
//            orderStatistics.setCount(countMap.getOrDefault(status, 0L));
//            orderStatisticsVOList.add(orderStatistics);
//        }
//        // 按数量降序排序（修复：原比较 b 与 b 永远相等，无法排序）
//        orderStatisticsVOList.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return null;
    }

    @Override
    public List<OrderStatisticsVO> todayOrder() {
        // 今日 0 点与明天 0 点作为时间区间
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        // 今日订单（按创建时间过滤）
        List<FlowerOrderDetail> orders = super.lambdaQuery()
                .ge(FlowerOrderDetail::getCreateTime, start)
                .lt(FlowerOrderDetail::getCreateTime, end)
                .list();
        // 通过今日订单的 id 关联查询已支付的支付记录，统计实收金额

        return null;
    }
}
