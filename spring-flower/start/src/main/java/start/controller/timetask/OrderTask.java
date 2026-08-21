//package start.controller.timetask;
//
//import lombok.extern.slf4j.Slf4j;
//import model.entity.Order;
//import model.enums.DeliveryStatusEnum;
//import model.enums.OrderStatusEnum;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import service.OrderService;
//
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//@Slf4j
//public class OrderTask {
//    @Autowired
//    private OrderService orderService;
//    /**
//     * 帮助处理订单
//     */
////    @Scheduled 使用 6 字段 cron 表达式（秒 分 时 日 月 周）
////  1	秒	0-59	*任意 / */n每隔 n 秒
////  2	分	0-59	*任意 / */n每隔 n 分钟
////  3	时	0-23	*任意小时
////  4	日	1-31	*任意日期
////  5	月	1-12	*任意月份
////  6	周	1 (周日)-7 (周六)	? 不指定（和日互斥）
//    //每60分钟触发一次
//    @Scheduled(cron = "0 0 * * * ?")
//    public void processTimeoutOrder(){
//        List<Order> ordersList1 = orderService.lambdaQuery()
//                .eq(Order::getStatus, OrderStatusEnum.GO).list();
//        if (!ordersList1.isEmpty()) {
//            log.info("有待处理订单：{}", ordersList1);
//        }
//        List<Order> ordersList2 = orderService.lambdaQuery()
//                .eq(Order::getDeliveryStatusEnum, DeliveryStatusEnum.NOW).list();
//        for (Order orders : ordersList2) {
//            LocalDateTime now = LocalDateTime.now();
//            if (orders.getStartDeliveryTime() != null && now.isAfter(orders.getStartDeliveryTime())) {
//                    log.info("id为{}订单需要开始派送了", orders.getId());
//            }
//        }
//
//    }
//}
