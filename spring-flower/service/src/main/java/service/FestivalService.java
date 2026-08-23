package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FestivalDTO;
import model.dto.FestivalPageDTO;
import model.dto.FlowerPageDTO;
import model.entity.Festival;
import model.vo.FestivalVO;

import java.util.List;

/**
 * 芊店的festival Service（对应 festival 表）
 */

public interface FestivalService extends IService<Festival> {

    FestivalVO readCache(Long id);

    void updateCache(FestivalDTO festivalDTO);

    void deleteCache(List<Long> ids);

    FestivalDTO create(FestivalDTO festivalDTO);

    List<FestivalVO> readPage(FestivalPageDTO festivalPageDTO);
}
