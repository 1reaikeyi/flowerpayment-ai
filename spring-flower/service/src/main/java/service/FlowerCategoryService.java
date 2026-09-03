package service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.result.PageResult;
import model.dto.FlowerCategoryPageDTO;
import model.dto.FlowerCategoryDTO;
import model.entity.FlowerCategory;
import model.vo.FestivalVO;
import model.vo.FlowerCategoryVO;
import model.vo.FlowerVO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * 花店分类 Service（对应 flower_category 表）
 */

public interface FlowerCategoryService extends IService<FlowerCategory> {

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    FlowerCategoryDTO create(FlowerCategoryDTO flowerCategoryDTO);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FlowerCategoryVO> readByType(Long type);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    PageResult<FlowerCategoryVO> readPage(FlowerCategoryPageDTO flowerCategoryPageDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void updateByObject(FlowerCategoryDTO categoryDTO);

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    void deleteById(List<Long> ids);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FlowerVO> readFlower(Long categoryId);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FestivalVO> readFestival(Long categoryId);
}
