package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FlowerDTO;
import model.entity.Flower;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 花店 Service（对应 flower 表）
 */
@Service
public interface FlowerService extends IService<Flower> {

    Flower readCache(Long id);

    void updateCache(FlowerDTO flowerDTO);

    void deleteCache(List<Long> ids);

    void deleteCacheById(Long id);
}
