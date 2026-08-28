//package start.controller.statistics;
//
//import common.enums.OperationEnum;
//import common.result.Result;
//import model.entity.*;
//import model.enums.OrderStatusEnum;
//import model.enums.PayStatusEnum;
//import model.vo.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import service.*;
//import start.aop.OperationLogging;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 统计模块控制器
// * 提供菜品/套餐销量、订单状态、今日营业情况及热销榜单等统计查询
// */
//@RestController
//@RequestMapping("admin/statistics")
//public class Statistics {
//    @Autowired
//    private FlowerService flowerService;
//    @Autowired
//    private FestivalService festivalService;
//    
//    private static final Integer TOP_NUMBER = 7;
//
//    // 菜品销量：按 dish_id 聚合订单明细中的销量与金额
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/dishSale")
//    public Result dishSale() {
//        // 查询所有菜品订单明细（dish_id 不为空表示为菜品订单）
//        List<FlowerOrderDetail> details = orderDetailService.lambdaQuery()
//                .isNotNull(FlowerOrderDetail::getFlowerId)
//                .list();
//        // 按 dish_id 分组累计销量和金额
//        Map<Long, Long> numberMap = new HashMap<>();
//        Map<Long, BigDecimal> amountMap = new HashMap<>();
//        for (FlowerOrderDetail d : details) {
//            numberMap.merge(d.getFlowerId(), d.getNumber(), Long::sum);
//            amountMap.merge(d.getFlowerId(), d.getAmount(), BigDecimal::add);
//        }
//        List<DishStatisticsVO> dishStatisticsVOList = new ArrayList<>();
//        // 组装结果：菜品名称 + 销量 + 金额
//        numberMap.forEach((dishId, number) -> {
//            DishStatisticsVO dishStatisticsVO = new DishStatisticsVO();
//            dishStatisticsVO.setDishId(dishId);
//            Flower dish flower = flowerService.readCache(dishId);
//            dishStatisticsVO.setName(dish != null ? flower.getName() : null);
//            dishStatisticsVO.setDishId(dish != null ?.getId() : null);
//            dishStatisticsVO.setNumber(number);
//            dishStatisticsVO.setAmount(amountMap.getOrDefault(dishId, BigDecimal.ZERO));
//            // 修复：原代码漏了将对象加入列表，导致菜品销量接口返回空列表
//            dishStatisticsVOList.add(dishStatisticsVO);
//        });
//        // 按销量降序排序（修复：原比较 b 与 b 永远相等，无法排序）
//        dishStatisticsVOList.sort((a, b) -> Long.compare(b.getNumber(), a.getNumber()));
//        return Result.success(dishStatisticsVOList);
//    }
//
//    // 套餐销量：按 plan_id 聚合订单明细中的销量与金额
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/planSale")
//    public Result planSale() {
//        List<FlowerOrderDetail> details = orderDetailService.lambdaQuery()
//                .isNotNull(FlowerOrderDetail::getFestivalId)
//                .list();
//        Map<Long, Long> numberMap = new HashMap<>();
//        Map<Long, BigDecimal> amountMap = new HashMap<>();
//        for (OrderDetail d : details) {
//            numberMap.merge(d.getPlanId(), d.getNumber(), Long::sum);
//            amountMap.merge(d.getPlanId(), d.getAmount(), BigDecimal::add);
//        }
//        List<PlanStatisticsVO> planStatisticsVOList = new ArrayList<>();
//        numberMap.forEach((festivalId, number) -> {
//            PlanStatisticsVO planStatistics = new PlanStatisticsVO();
//            planStatistics.setPlanId(planId);
//            Festival festival = festivalService.readCache(planId);
//            planStatistics.setName(plan != null ? festival.getName() : null);
//            planStatistics.setNumber(number);
//            planStatistics.setAmount(amountMap.getOrDefault(planId, BigDecimal.ZERO));
//            planStatisticsVOList.add(planStatistics);
//        });
//        // 按销量降序排序（修复：原比较 b 与 b 永远相等，无法排序）
//        planStatisticsVOList.sort((a, b) -> Long.compare(b.getNumber(), a.getNumber()));
//        return Result.success(planStatisticsVOList);
//    }
//
//    // 订单统计：按订单状态统计数量
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/order")
//    public Result order() {
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
//        return Result.success(orderStatisticsVOList);
//    }
//
//    // 今日统计：今日订单数、已支付订单数、今日营业额
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/today")
//    public Result today() {
//        // 今日 0 点与明天 0 点作为时间区间
//        LocalDateTime start = LocalDate.now().atStartOfDay();
//        LocalDateTime end = start.plusDays(1);
//        // 今日订单（按创建时间过滤）
//        List<Order> orders = orderService.lambdaQuery()
//                .ge(Order::getCreateTime, start)
//                .lt(Order::getCreateTime, end)
//                .list();
//        // 通过今日订单的 id 关联查询已支付的支付记录，统计实收金额
//        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
//        BigDecimal todayAmount = BigDecimal.ZERO;
//        long paidCount = 0;
//        if (!orderIds.isEmpty()) {
//            List<OrderPay> pays = orderPayService.lambdaQuery()
//                    .in(OrderPay::getOrderId, orderIds)
//                    .eq(OrderPay::getPayStatus, PayStatusEnum.PAID)
//                    .list();
//            todayAmount = pays.stream()
//                    .map(OrderPay::getAmount)
//                    .filter(Objects::nonNull)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            paidCount = pays.size();
//        }
//        TodayStatisticsVO todayStatisticsVO = new TodayStatisticsVO();
//        todayStatisticsVO.setOrderCount(orders.size());   // 今日下单数
//        todayStatisticsVO.setPaidCount(paidCount);        // 今日已支付订单数
//        todayStatisticsVO.setTodayAmount(todayAmount);         // 今日营业额
//        return Result.success(todayStatisticsVO);
//    }
//
//    // 热销菜品榜,按销量取前 TOP_NUMBER
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/topDish")
//    public Result topDish() {
//        List<OrderDetail> details = orderDetailService.lambdaQuery()
//                .isNotNull(OrderDetail::getDishId)
//                .list();
//        // 按 dish_id 分组累计销量
//        Map<Long, Long> numberMap = new HashMap<>();
//        for (OrderDetail d : details) {
//            numberMap.merge(d.getDishId(), d.getNumber(), Long::sum);
//        }
//        // 降序排序取前
//        List<TopStatisticsVO> dishStatisticsVOList = new ArrayList<>();
//        numberMap.entrySet().stream()
//                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
//                .limit(TOP_NUMBER)
//                .forEach(e -> {
//                    TopStatisticsVO dishStatistics = new TopStatisticsVO();
//                    Dish dish = dishService.readCache(e.getKey());
//                    dishStatistics.setId(e.getKey());
//                    dishStatistics.setName(dish != null ? dish.getName() : null);
//                    dishStatistics.setNumber(e.getValue());
//                    dishStatisticsVOList.add(dishStatistics);
//                });
//        return Result.success(dishStatisticsVOList);
//    }
//
//    // 热销套餐榜：按销量取前 TOP_NUMBER
//    @OperationLogging(operation = OperationEnum.READ)
//    @GetMapping("/topPlan")
//    public Result topPlan() {
//        List<OrderDetail> details = orderDetailService.lambdaQuery()
//                .isNotNull(OrderDetail::getPlanId)
//                .list();
//        Map<Long, Long> numberMap = new HashMap<>();
//        for (OrderDetail d : details) {
//            numberMap.merge(d.getPlanId(), d.getNumber(), Long::sum);
//        }
//        List<TopStatisticsVO> planStatisticsVOList = new ArrayList<>();
//        numberMap.entrySet().stream()
//                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
//                .limit(TOP_NUMBER)
//                .forEach(e -> {
//                    TopStatisticsVO planStatistics = new TopStatisticsVO();
//                    Plan plan = planService.readCache(e.getKey());
//                    planStatistics.setId(e.getKey());
//                    planStatistics.setName(plan != null ? plan.getName() : null);
//                    planStatistics.setNumber(e.getValue());
//                    planStatisticsVOList.add(planStatistics);
//                });
//        return Result.success(planStatisticsVOList);
//    }
//
//}
