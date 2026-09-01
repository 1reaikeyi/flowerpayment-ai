package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FestivalDetailDTO;
import model.entity.FestivalDetail;
import model.vo.FestivalDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 芊店的festival关系 Service（对应 festival_detail 表）
 */

public interface FestivalDetailService extends IService<FestivalDetail> {

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    FestivalDetailDTO create(FestivalDetailDTO festivalDetailDTO);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    FestivalDetailVO readCache(Long id);

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    void updateCache(FestivalDetailDTO festivalDetailDTO);

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    void deleteCache(List<Long> ids);

}
