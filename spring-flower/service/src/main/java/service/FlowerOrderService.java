package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FlowerOrderPageDTO;
import model.entity.FlowerOrder;
import model.vo.FlowerOrderVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单 Service（对应 flower_order 表）
 */

public interface FlowerOrderService extends IService<FlowerOrder> {
    FlowerOrderVO readById(Long id);

    List<FlowerOrderVO> readPage(FlowerOrderPageDTO flowerOrderPageDTO);

    void update3(Long id);

    void update4(Long id);

    void update5(Long id);

    void update6(Long id);

    void update7(Long id);

    void update8(Long id);

    void update1();

    void update2();
}
