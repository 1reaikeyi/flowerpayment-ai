package service;

import com.baomidou.mybatisplus.extension.service.IService;
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

    FlowerCategoryDTO create(FlowerCategoryDTO flowerCategoryDTO);

    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FlowerCategoryVO> readByType(Long type);
    @PreAuthorize("hasAuthority('ROLE_USER')")
    List<FlowerCategoryVO> readPage(FlowerCategoryPageDTO flowerCategoryPageDTO);

    void updateByObject(FlowerCategoryDTO categoryDTO);

    void deleteById(List<Long> ids);

    List<FlowerVO> readFlower(Long categoryId);

    List<FestivalVO> readFestival(Long categoryId);
}
