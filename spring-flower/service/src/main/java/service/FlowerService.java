package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FlowerDTO;
import model.dto.FlowerPageDTO;
import model.entity.Flower;
import model.vo.FlowerDetailVO;
import model.vo.FlowerVO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 花店 Service（对应 flower 表）
 */

public interface FlowerService extends IService<Flower> {

    @PreAuthorize("hasAuthority('ROLE_USER')")
    FlowerVO readCache(Long id);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void updateCache(FlowerDTO flowerDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void deleteCache(List<Long> ids);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FlowerVO> readPage(FlowerPageDTO flowerPageDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    FlowerDTO create(FlowerDTO flowerDTO);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FlowerDetailVO> readFestivalDetail(Long id);
}
