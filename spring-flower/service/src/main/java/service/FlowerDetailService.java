package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FlowerDTO;
import model.dto.FlowerDetailDTO;
import model.entity.FlowerDetail;
import model.vo.FlowerDetailVO;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 花店关系 Service（对应 flower_detail 表）
 */

public interface FlowerDetailService extends IService<FlowerDetail> {
    FlowerDetailDTO create(FlowerDetailDTO flowerDetailDTO);

    FlowerDetailVO readCache(Long id);

    void updateCache(FlowerDTO flowerDTO);

    void deleteCache(List<Long> ids);
}
