package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FlowerOrderDetailMapper;
import model.entity.FlowerOrderDetail;
import org.springframework.stereotype.Service;

@Service
public class FlowerOrderDetailService extends ServiceImpl<FlowerOrderDetailMapper, FlowerOrderDetail> implements service.FlowerOrderDetailService {
}
