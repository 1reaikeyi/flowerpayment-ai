package service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.result.PageResult;
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

    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_EMP') or hasAuthority('ROLE_ADMIN')")
    FlowerVO readCache(Long id);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void updateCache(FlowerDTO flowerDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void deleteCache(List<Long> ids);

    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_EMP') or hasAuthority('ROLE_ADMIN')")
    PageResult<FlowerVO> readPage(FlowerPageDTO flowerPageDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    FlowerDTO create(FlowerDTO flowerDTO);

    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_EMP') or hasAuthority('ROLE_ADMIN')")
    List<FlowerDetailVO> readFlowerDetail(Long id);
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_EMP') or hasAuthority('ROLE_ADMIN')")
    List<FlowerDetailVO> readOfObject(String object);
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_EMP') or hasAuthority('ROLE_ADMIN')")
    List<FlowerDetailVO> readOfOption(String option);

}
