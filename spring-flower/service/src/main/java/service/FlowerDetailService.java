package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FlowerDTO;
import model.dto.FlowerDetailDTO;
import model.entity.FlowerDetail;
import model.vo.FlowerDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 花店关系 Service（对应 flower_detail 表）
 */

public interface FlowerDetailService extends IService<FlowerDetail> {

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    FlowerDetailDTO create(FlowerDetailDTO flowerDetailDTO);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    FlowerDetailVO readCache(Long id);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void updateCache(FlowerDetailDTO flowerDetailDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void deleteCache(List<Long> ids);

}
