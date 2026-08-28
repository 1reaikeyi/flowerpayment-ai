package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FlowerOrderMapper;
import model.dto.FlowerOrderPageDTO;
import model.entity.FlowerOrder;
import model.vo.FlowerOrderVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowerOrderServiceImpl extends ServiceImpl<FlowerOrderMapper, FlowerOrder> implements service.FlowerOrderService {
    @Override
    public FlowerOrderVO readById(Long id) {
        return null;
    }

    @Override
    public List<FlowerOrderVO> readPage(FlowerOrderPageDTO flowerOrderPageDTO) {
        return List.of();
    }

    @Override
    public void update3(Long id) {

    }

    @Override
    public void update4(Long id) {

    }

    @Override
    public void update5(Long id) {

    }

    @Override
    public void update6(Long id) {

    }

    @Override
    public void update7(Long id) {

    }

    @Override
    public void update8(Long id) {

    }
}
