package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.FestivalDTO;
import model.dto.FestivalPageDTO;
import model.dto.FlowerPageDTO;
import model.entity.Festival;
import model.vo.FestivalDetailVO;
import model.vo.FestivalVO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 芊店的festival Service（对应 festival 表）
 */

public interface FestivalService extends IService<Festival> {

    @PreAuthorize("hasAuthority('ROLE_USER')")
    FestivalVO readCache(Long id);
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void updateCache(FestivalDTO festivalDTO);
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void deleteCache(List<Long> ids);
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    FestivalDTO create(FestivalDTO festivalDTO);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FestivalVO> readPage(FestivalPageDTO festivalPageDTO);
    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FestivalDetailVO> readFestivalDetail(Long id);
    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FestivalDetailVO> readFlower(Long id);
}
