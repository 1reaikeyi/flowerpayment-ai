package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.FestivalDetailMapper;
import model.dto.FestivalDetailDTO;
import model.entity.FestivalDetail;
import model.vo.FestivalDetailVO;
import org.springframework.stereotype.Service;
import service.FestivalDetailService;

import java.util.List;

@Service
public class FestivalDetailServiceImpl extends ServiceImpl<FestivalDetailMapper, FestivalDetail> implements FestivalDetailService {
    @Override
    public FestivalDetailDTO create(FestivalDetailDTO festivalDetailDTO) {
        return null;
    }

    @Override
    public FestivalDetailVO readCache(Long id) {
        return null;
    }

    @Override
    public void updateCache(FestivalDetailDTO festivalDetailDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }
}
