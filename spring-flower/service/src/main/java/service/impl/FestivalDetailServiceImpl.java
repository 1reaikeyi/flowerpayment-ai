package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FestivalDetailMapper;
import model.entity.FestivalDetail;
import org.springframework.stereotype.Service;
import service.FestivalDetailService;
@Service
public class FestivalDetailServiceImpl extends ServiceImpl<FestivalDetailMapper, FestivalDetail> implements FestivalDetailService {
}
