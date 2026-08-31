package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.entity.FlowerOrderDetail;
import model.vo.statistics.StatisticsVO;
import model.vo.statistics.OrderStatisticsVO;
import model.vo.statistics.TopStatisticsVO;

import java.util.List;

/**
 * 订单详情 Service（对应 flower_order_detail 表）
 */

public interface FlowerOrderDetailService extends IService<FlowerOrderDetail> {
    List<StatisticsVO> flowerSale();

    List<StatisticsVO> festivalSale();

    List<TopStatisticsVO> top1();

    List<TopStatisticsVO> top2();

    List<OrderStatisticsVO> order();

    List<OrderStatisticsVO> todayOrder();
}
