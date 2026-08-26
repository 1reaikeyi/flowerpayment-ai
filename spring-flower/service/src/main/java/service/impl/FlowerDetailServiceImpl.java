package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import mapper.FlowerDetailMapper;
import model.dto.FlowerDTO;
import model.dto.FlowerDetailDTO;
import model.entity.FlowerDetail;
import model.vo.FlowerDetailVO;
import org.springframework.stereotype.Service;
import service.FlowerDetailService;

import java.util.List;

@Service
@Slf4j
public class FlowerDetailServiceImpl extends ServiceImpl<FlowerDetailMapper, FlowerDetail> implements FlowerDetailService {

    @Override
    public FlowerDetailDTO create(FlowerDetailDTO flowerDetailDTO) {
        return null;
    }

    @Override
    public FlowerDetailVO readCache(Long id) {
        return null;
    }

    @Override
    public void updateCache(FlowerDTO flowerDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }
}
