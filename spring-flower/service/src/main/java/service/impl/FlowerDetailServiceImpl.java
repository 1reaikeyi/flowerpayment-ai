package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import mapper.FlowerDetailMapper;
import model.entity.FlowerDetail;
import org.springframework.stereotype.Service;
import service.FlowerDetailService;

@Service
@Slf4j
public class FlowerDetailServiceImpl extends ServiceImpl<FlowerDetailMapper, FlowerDetail> implements FlowerDetailService {

}
