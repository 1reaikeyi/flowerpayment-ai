package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FestivalDTO;
import model.entity.Festival;
import model.entity.FestivalDetail;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 芊店的festival Service（对应 festival 表）
 */

public interface FestivalService extends IService<Festival> {

    Festival readCache(Long id);

    void updateCache(FestivalDTO festivalDTO);

    void deleteCache(List<Long> ids);

}
