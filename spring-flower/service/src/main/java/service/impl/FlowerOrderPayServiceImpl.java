package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FlowerOrderPayMapper;
import model.entity.FlowerOrderPay;
import org.springframework.stereotype.Service;

@Service
public class FlowerOrderPayServiceImpl extends ServiceImpl<FlowerOrderPayMapper, FlowerOrderPay> implements service.FlowerOrderPayService {
}
