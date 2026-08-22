package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FestivalMapper;
import model.dto.FestivalDTO;
import model.entity.Festival;
import org.springframework.stereotype.Service;
import service.FestivalService;

import java.util.List;
@Service
public class FestivalServiceImpl extends ServiceImpl<FestivalMapper, Festival> implements FestivalService {
    @Override
    public void updateCache(FestivalDTO festivalDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }

    @Override
    public Festival readCache(Long id) {
        return null;
    }
}
