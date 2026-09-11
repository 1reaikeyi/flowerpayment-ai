package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.entity.FlowerOrderDetail;
import model.entity.User;
import model.vo.statistics.StatisticsVO;
import model.vo.statistics.OrderStatisticsVO;
import model.vo.statistics.TopStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 订单详情 Service（对应 flower_order_detail 表）
 */

public interface FlowerOrderDetailService extends IService<FlowerOrderDetail> {

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    List<StatisticsVO> flowerSale();
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    List<StatisticsVO> festivalSale();
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    List<TopStatisticsVO> top1();
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    List<TopStatisticsVO> top2();
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    List<OrderStatisticsVO> order();
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN') or hasAnyAuthority('ROLE_EMP')")
    List<OrderStatisticsVO> todayOrder();
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    List<User> userData();
}
