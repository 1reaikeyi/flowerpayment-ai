package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FlowerDTO;
import model.dto.FlowerPageDTO;
import model.entity.Flower;
import model.vo.FlowerVO;

import java.util.List;

/**
 * 花店 Service（对应 flower 表）
 */

public interface FlowerService extends IService<Flower> {

    FlowerVO readCache(Long id);

    void updateCache(FlowerDTO flowerDTO);

    void deleteCache(List<Long> ids);

    List<FlowerVO> readPage(FlowerPageDTO flowerPageDTO);

    FlowerDTO create(FlowerDTO flowerDTO);
}
