package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FlowerOrderMapper;
import model.entity.FlowerOrder;
import org.springframework.stereotype.Service;

@Service
public class FlowerOrderService extends ServiceImpl<FlowerOrderMapper, FlowerOrder> implements service.FlowerOrderService {
}
