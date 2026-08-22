package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FlowerDetailMapper;
import model.entity.FlowerDetail;
import org.springframework.stereotype.Service;
import service.FlowerDetailService;
import service.FlowerService;
@Service
public class FlowerDetailServiceImpl extends ServiceImpl<FlowerDetailMapper, FlowerDetail> implements FlowerDetailService {
}
