package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FlowerMapper;
import model.dto.FlowerDTO;
import model.entity.Flower;
import org.springframework.stereotype.Service;
import service.FlowerService;

import java.util.Collection;
import java.util.List;

@Service
public class FlowerServiceImpl extends ServiceImpl<FlowerMapper, Flower> implements FlowerService {

    @Override
    public Flower readCache(Long id) {
        return null;
    }

    @Override
    public void updateCache(FlowerDTO flowerDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }


}
